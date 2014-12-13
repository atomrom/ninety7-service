package com.atomrom.ninety7.service.dao;

public class DigestItem {

	public String url;
	public String title;
	public String abstr;
	public String keywords;

	public DigestItem(String url, String title, String abstr, String keywords) {
		this.url = url;
		this.title = title;
		this.abstr = abstr;
		this.keywords = keywords;
	}

	@Override
	public String toString() {
		return "DigestItem [url=" + url + ", title=" + title + ", abstr="
				+ abstr + ", keywords=" + keywords + "]";
	}
	
}
