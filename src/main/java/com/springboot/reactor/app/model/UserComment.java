package com.springboot.reactor.app.model;

import java.io.Serializable;

public class UserComment implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private User user;
	private Comment comment;

	public UserComment(User user, Comment comment) {
		this.user = user;
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "UserComment [user=" + user + ", comment=" + comment + "]";
	}

}
