package com.storyworld.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.storyworld.domain.elastic.CommentContent;
import com.storyworld.domain.json.Request;
import com.storyworld.domain.json.Response;
import com.storyworld.domain.json.enums.StatusMessage;
import com.storyworld.domain.sql.Comment;
import com.storyworld.domain.sql.LikeTypeComment;
import com.storyworld.domain.sql.Story;
import com.storyworld.domain.sql.User;
import com.storyworld.domain.sql.enums.LikeType;
import com.storyworld.repository.elastic.CommentContentRepository;
import com.storyworld.repository.sql.CommentRepository;
import com.storyworld.repository.sql.LikeTypeCommentRepository;
import com.storyworld.repository.sql.StoryRepository;
import com.storyworld.repository.sql.UserRepository;
import com.storyworld.service.CommentService;
import com.storyworld.service.JSONService;

@Service
public class CommentServiceImpl implements CommentService {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private StoryRepository storyRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CommentContentRepository commentContentRepository;

	@Autowired
	private LikeTypeCommentRepository likeTypeCommentRepository;

	@Autowired
	private JSONService jsonService;

	private static final Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void get(Long idStory, int page, int pageSize, Response response) {
		Story story = storyRepository.findOne(idStory);
		if (story != null && page > -1 && pageSize > 0) {
			Page<Comment> comments = commentRepository.findByStory(story, new PageRequest(page, pageSize));
			List<CommentContent> commentsContent = new LinkedList<>();
			comments.forEach(x -> commentsContent.add(commentContentRepository.findOne(x.get_id())));
			commentsContent.sort((CommentContent o1, CommentContent o2) -> o2.getDate().compareTo(o1.getDate()));
			jsonService.prepareResponseForComment(response, null, null, commentsContent, null, true);
		} else
			jsonService.prepareErrorResponse("INCORRECT_DATA");
	}

	@Override
	public void save(Request request, Response response) {
		Optional<User> user = userRepository.findByToken(request.getToken());
		user.ifPresent(x -> {
			Story story = storyRepository.findOne(request.getStory().getId());
			Comment comment = commentRepository.findByAuthorAndStory(x, story);
			CommentContent commentContent = request.getCommentContent();
			if (story != null && commentContent != null && comment == null) {
				comment = new Comment(x, story);
				try {
					x.setLastActionTime(LocalDateTime.now());
					userRepository.save(x);
					x = new User(x.getId(), x.getName());
					commentContent.setAuthor(x);
					commentContent.setStoryId(story.getId());
					commentContent.setLikes(0);
					commentContent.setDislikes(0);
					commentContent.setDate(LocalDateTime.now().format(FORMATTER).toString());
					commentContent = commentContentRepository.save(commentContent);
					comment.set_id(commentContent.getId());
					commentRepository.save(comment);
					jsonService.prepareResponseForComment(response, StatusMessage.SUCCESS, "ADDED", null,
							commentContent, true);
				} catch (Exception e) {
					LOG.error(e.toString());
					jsonService.prepareErrorResponse("INCORRECT_DATA");
				}
			} else {
				if (comment != null)
					jsonService.prepareErrorResponse("UNIQUE_COMMENT");
				else
					jsonService.prepareErrorResponse("INCORRECT_DATA");
			}
		});

	}

	@Override
	public void update(Request request, Response response) {
		Optional<User> user = userRepository.findByToken(request.getToken());
		user.ifPresent(x -> {
			Comment comment = commentRepository.findBy_id(request.getCommentContent().getId());
			if (comment != null && request.getCommentContent() != null) {
				CommentContent commentContent = commentContentRepository.findOne(comment.get_id());
				commentContent.setEdited(true);
				commentContent.setContent(request.getCommentContent().getContent());
				commentContent.setDate(LocalDateTime.now().format(FORMATTER).toString());
				commentContent = commentContentRepository.save(commentContent);
				x.setLastActionTime(LocalDateTime.now());
				userRepository.save(x);
				jsonService.prepareResponseForComment(response, StatusMessage.SUCCESS, "UPDATED", null, commentContent,
						true);
			} else
				jsonService.prepareErrorResponse("INCORRECT_DATA");
		});

	}

	@Override
	public void delete(Request request, Response response) {
		Optional<User> user = userRepository.findByToken(request.getToken());
		Comment comment = commentRepository.findBy_id(request.getComment().get_id());
		if (comment != null) {
			CommentContent commentContent = commentContentRepository.findOne(comment.get_id());
			commentContentRepository.delete(commentContent);
			commentRepository.delete(comment);
			user.get().setLastActionTime(LocalDateTime.now());
			userRepository.save(user.get());
			jsonService.prepareResponseForComment(response, StatusMessage.SUCCESS, "DELETED", null, null, true);
		} else
			jsonService.prepareErrorResponse("INCORRECT_DATA");
	}

	@Override
	public synchronized void like(Request request, Response response) {
		Optional<User> user = userRepository.findByToken(request.getToken());
		user.ifPresent(x -> {
			CommentContent commentContent = commentContentRepository.findOne(request.getCommentContent().getId());
			if (commentContent != null) {
				Comment comment = commentRepository.findBy_id(commentContent.getId());
				LikeTypeComment likeTypeComment = likeTypeCommentRepository.findByUserAndComment(x, comment);
				if ((likeTypeComment != null && likeTypeComment.getLikeType().equals(LikeType.DISLIKE))
						|| likeTypeComment == null) {
					if (likeTypeComment != null && likeTypeComment.getLikeType().equals(LikeType.DISLIKE)) {
						int dislike = commentContent.getDislikes() - 1;
						commentContent.setDislikes(dislike);
						likeTypeComment.setLikeType(LikeType.LIKE);
						likeTypeCommentRepository.save(likeTypeComment);
					} else {
						likeTypeComment = new LikeTypeComment(x, comment, LikeType.LIKE);
						likeTypeCommentRepository.save(likeTypeComment);
					}
					int like = commentContent.getLikes() + 1;
					commentContent.setLikes(like);
					commentContent = commentContentRepository.save(commentContent);
					x.setLastActionTime(LocalDateTime.now());
					userRepository.save(x);
					jsonService.prepareResponseForComment(response, StatusMessage.SUCCESS, "LIKED", null,
							commentContent, true);
				} else
					jsonService.prepareSimpleResponse(response, true, StatusMessage.WARNING, "UNIQUE_LIKE");
			} else
				jsonService.prepareErrorResponse("INCORRECT_DATA");
		});
	}

	@Override
	public synchronized void dislike(Request request, Response response) {
		Optional<User> user = userRepository.findByToken(request.getToken());
		user.ifPresent(x -> {
			CommentContent commentContent = commentContentRepository.findOne(request.getCommentContent().getId());
			if (commentContent != null) {
				Comment comment = commentRepository.findBy_id(commentContent.getId());
				LikeTypeComment likeTypeComment = likeTypeCommentRepository.findByUserAndComment(x, comment);
				if ((likeTypeComment != null && likeTypeComment.getLikeType().equals(LikeType.LIKE))
						|| likeTypeComment == null) {
					if (likeTypeComment != null && likeTypeComment.getLikeType().equals(LikeType.LIKE)) {
						int like = commentContent.getLikes() - 1;
						commentContent.setLikes(like);
						likeTypeComment.setLikeType(LikeType.DISLIKE);
						likeTypeCommentRepository.save(likeTypeComment);
					} else {
						likeTypeComment = new LikeTypeComment(x, comment, LikeType.DISLIKE);
						likeTypeCommentRepository.save(likeTypeComment);
					}
					int dislike = commentContent.getDislikes() + 1;
					commentContent.setDislikes(dislike);
					commentContent = commentContentRepository.save(commentContent);
					x.setLastActionTime(LocalDateTime.now());
					userRepository.save(x);
					jsonService.prepareResponseForComment(response, StatusMessage.SUCCESS, "DISLIKED", null,
							commentContent, true);
				} else
					jsonService.prepareSimpleResponse(response, true, StatusMessage.WARNING, "UNIQUE_LIKE");
			} else
				jsonService.prepareErrorResponse("INCORRECT_DATA");
		});
	}
}
