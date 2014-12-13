package com.atomrom.ninety7.service;

import java.util.List;

public class GoogleResults {

	private ResponseData responseData;

	public ResponseData getResponseData() {
		return responseData;
	}

	public void setResponseData(ResponseData responseData) {
		this.responseData = responseData;
	}

	public String toString() {
		return "ResponseData[" + responseData + "]";
	}

	static class ResponseData {
		private List<Result> results;

		private Cursor cursor;

		public List<Result> getResults() {
			return results;
		}

		public void setResults(List<Result> results) {
			this.results = results;
		}

		public Cursor getCursor() {
			return cursor;
		}

		public void setCursor(Cursor cursor) {
			this.cursor = cursor;
		}

	}

	static class Result {
		private String url;
		private String title;

		public String getUrl() {
			return url;
		}

		public String getTitle() {
			return title;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String toString() {
			return "Result[url:" + url + ",title:" + title + "]";
		}
	}

	static class Cursor {
		private String moreResultsUrl;

		public String getMoreResultsUrl() {
			return moreResultsUrl;
		}

		public void setMoreResultsUrl(String moreResultsUrl) {
			this.moreResultsUrl = moreResultsUrl;
		}

	}
}
