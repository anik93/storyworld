package com.storyworld.domain.sql;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.storyworld.domain.sql.enums.LikeType;

import lombok.Data;

@Data
@Entity
@Table(name = "LIKETYPECOMMENT")
public class LikeTypeComment implements Serializable {

	private static final long serialVersionUID = 4448839057155542878L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userId")
	@NotNull
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "commentId")
	@NotNull
	private Comment comment;

	@Enumerated(EnumType.STRING)
	@NotNull
	private LikeType likeType;

	public LikeTypeComment() {
	}

	public LikeTypeComment(User user, Comment comment, LikeType likeType) {
		this.user = user;
		this.comment = comment;
		this.likeType = likeType;
	}

}
