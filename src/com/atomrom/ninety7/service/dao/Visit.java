package com.atomrom.ninety7.service.dao;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

public class Visit {

	public int id;

	public User user;

	public String url;
	public String title;
	public String content;

	public Integer duration;

	public Visit(HttpServletRequest req) {
		url = req.getParameter("urlVisited");
		duration = Integer.parseInt(req.getParameter("visitDuration"));
		title = req.getParameter("title");
		content = req.getParameter("content");
	}

	public Visit(Integer id, User user, String url, String title, String content) {
		if (id != null) {
			this.id = id;
		}

		this.user = user;

		this.url = url;
		this.title = title;
		this.content = content;
	}

	public Visit(Entity entity) {
		this((Integer) entity.getProperty("id"), (User) entity
				.getProperty("user"), (String) entity.getProperty("url"),
				((Text) entity.getProperty("title")).getValue(), ((Text) entity
						.getProperty("content")).getValue());
	}

	@Override
	public String toString() {
		return "VisitedPage [id=" + id + ", user=" + user + ", url=" + url
				+ ", title=" + title + ", content=" + content + ", duration="
				+ duration + "]";
	}

}
