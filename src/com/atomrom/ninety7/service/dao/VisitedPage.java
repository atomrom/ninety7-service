package com.atomrom.ninety7.service.dao;

public class VisitedPage {

	public int id;

	public String url;
	public String title;
	public String content;

	public VisitedPage(Integer id, String url, String title, String content) {
		this.url = url;
		this.title = title;
		this.content = content;
	}

	@Override
	public String toString() {
		return "VisitedPage [id=" + id + ", url=" + url + ", title=" + title
				+ ", content=" + content + "]";
	}

}
