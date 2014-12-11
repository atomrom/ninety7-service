package com.atomrom.ninety7.service.dao;

public class DigestItem {

	public String url;
	public String title;
	public String abstr;

	public DigestItem(String url, String title, String abstr) {
		this.url = url;
		this.title = title;
		this.abstr = abstr;
	}

	@Override
	public String toString() {
		return "DigestItem [url=" + url + ", title=" + title + ", abstr="
				+ abstr + "]";
	}

}
