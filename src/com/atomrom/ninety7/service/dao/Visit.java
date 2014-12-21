package com.atomrom.ninety7.service.dao;

import javax.servlet.http.HttpServletRequest;

import com.atomrom.ninety7.service.Ninety7CollectorServlet;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

public class Visit {

	public int id;

	public User user;

	public String url;
	public String title;
	public String content;

	public String metaKeywords;
	public String metaDescription;

	public Integer duration;

	public Visit(HttpServletRequest req) {
		url = req.getParameter("urlVisited");
		duration = Integer.parseInt(req.getParameter("visitDuration"));
		title = req.getParameter("title");
		content = req.getParameter("content");
		
		metaKeywords = req.getParameter(Ninety7CollectorServlet.REQUEST_PARAM_META_KEYWORDS);
		metaDescription = req.getParameter(Ninety7CollectorServlet.REQUEST_PARAM_META_DESCRIPTION);
	}

	public Visit(Integer id, User user, String url, String title,
			String content, String metaKeywords, String metaDescription) {
		if (id != null) {
			this.id = id;
		}

		this.user = user;

		this.url = url;
		this.title = title;
		this.content = content;

		this.metaKeywords = metaKeywords;
		this.metaDescription = metaDescription;
	}

	public Visit(Entity entity) {
		this((Integer) entity.getProperty("id"), (User) entity
				.getProperty("user"), (String) entity.getProperty("url"),
				((Text) entity.getProperty("title")).getValue(), ((Text) entity
						.getProperty("content")).getValue(),
				getStringValueOrNull(entity, VisitDao.META_KEYWORDS),
				getStringValueOrNull(entity, VisitDao.META_DESCRIPTION));
	}

	private static String getStringValueOrNull(Entity entity,
			String propertyName) {
		Object property = entity.getProperty(propertyName);
		if (property == null) {
			return null;
		}

		if (property instanceof Text) {
			return ((Text) property).getValue();
		}
		
		return property.toString();
	}

	@Override
	public String toString() {
		return "Visit [id=" + id + ", user=" + user + ", url=" + url
				+ ", title=" + title + ", content=" + content
				+ ", metaKeywords=" + metaKeywords + ", metaDescription="
				+ metaDescription + ", duration=" + duration + "]";
	}

}
