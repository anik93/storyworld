package com.storyworld.domain.json;

import java.util.List;

import com.storyworld.domain.sql.User;

public class Request {

	private String token;
	
	private User user;
	
	private List<FavouritePlaces> favouritePlaces;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<FavouritePlaces> getFavouritePlaces() {
		return favouritePlaces;
	}

	public void setFavouritePlaces(List<FavouritePlaces> favouritePlaces) {
		this.favouritePlaces = favouritePlaces;
	}
	
	
}