package com.springboot.reactor.app.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class User implements Serializable {

	private String name;
	private String subname;

	public User() {
		// TODO Auto-generated constructor stub
	}

	public User(String name, String subname) {
		super();
		this.name = name;
		this.subname = subname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubname() {
		return subname;
	}

	public void setSubname(String subname) {
		this.subname = subname;
	}
	
	@Override
	public String toString() {
	   return "User=[name="+this.name+", subname="+this.subname+"]";
	}
}
