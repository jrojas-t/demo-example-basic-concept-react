package com.springboot.reactor.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Comment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> comments;

	public Comment() {
		this.comments = new ArrayList<String>();
	}

	public void addComment(String comment) {
		this.comments.add(comment);
	}

	@Override
	public String toString() {
		return "comments=" + comments + "";
	}

}
