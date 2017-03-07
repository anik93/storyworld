package com.storyworld.service;

import com.storyworld.domain.json.Request;
import com.storyworld.domain.json.Response;
import com.storyworld.domain.sql.User;

public interface UserService {

	public void removeToken(User user);

	public void login(Request request, Response response);

	public void register(Request request, Response response);

	public Response getUsers(Response response);
}
