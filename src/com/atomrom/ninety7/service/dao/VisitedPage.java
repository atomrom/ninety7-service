package com.atomrom.ninety7.service.dao;

import com.google.appengine.api.users.User;

public class VisitedPage {

	public int id;

	public User user;

	public String url;
	public String title;
	public String content;

	public VisitedPage(Integer id, User user, String url, String title,
			String content) {
		if (id != null) {
			this.id = id;
		}

		this.user = user;

		this.url = url;
		this.title = title;
		this.content = content;
	}

	@Override
	public String toString() {
		return "VisitedPage [id=" + id + ", user=" + user + ", url=" + url
				+ ", title=" + title + ", content=" + content + "]";
	}

}
