package com.storyworld.service.impl;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import com.storyworld.domain.json.Request;
import com.storyworld.domain.json.Response;
import com.storyworld.service.CommentService;

@Service
public class CommentServiceImpl implements CommentService {
	
	@Override
	public void get(Request request, Response response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void save(Request request, Response response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Request request, Response response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Request request, Response response) {
		// TODO Auto-generated method stub

	}

}
