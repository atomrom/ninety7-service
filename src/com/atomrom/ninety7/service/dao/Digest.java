package com.atomrom.ninety7.service.dao;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class Digest {

	public String url;
	public String title;
	public String abstr;
	public String keywords;

	public long rank;
	public int fullAbstractHashCode;

	public boolean closed = false;

	public Digest(String url, String title, String abstr, String keywords, Long rank, Integer fullAbstractHashCode) {
		this.url = url;
		this.title = title;
		this.abstr = abstr;
		this.keywords = keywords;

		if (rank == null) {
			this.rank = 0;
		} else {
			this.rank = rank;
		}

		if (fullAbstractHashCode == null) {
			this.fullAbstractHashCode = 0;
		} else {
			this.fullAbstractHashCode = fullAbstractHashCode;
		}
	}

	public JSONObject toJson() throws JSONException {
		JSONObject digestItemJson = new JSONObject();

		digestItemJson.put("url", url);
		digestItemJson.put("title", title);
		digestItemJson.put("abstract", abstr);
		digestItemJson.put("keywords", keywords);

		return digestItemJson;
	}

	@Override
	public String toString() {
		return "Digest [url=" + url + ", title=" + title + ", abstr=" + abstr + ", keywords=" + keywords + ", rank=" + rank + ", fullAbstractHashCode="
				+ fullAbstractHashCode + ", closed=" + closed + "]";
	}

}
