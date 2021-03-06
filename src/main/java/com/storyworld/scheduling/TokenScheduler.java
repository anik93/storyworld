package com.storyworld.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.storyworld.repository.sql.UserRepository;
import com.storyworld.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenScheduler {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TokenScheduler.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Scheduled(fixedRate = 60000)
	public void removeToken() {
		userRepository.findByTokenNotNull().forEach(userService::removeToken);
	}
}
