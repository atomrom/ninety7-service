package com.atomrom.ninety7.service.search;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atomrom.ninety7.service.search.GoogleSearchResults.ResponseData;
import com.atomrom.ninety7.service.util.TextAnalyzer;
import com.google.gson.Gson;

public class GoogleSearch {
	
	private static final Logger logger = Logger.getLogger(GoogleSearch.class
			.getName());
	
	private static final String GOOGLE_SEARCH_URL = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	private static final String CHARSET = "UTF-8";
	
	
	public static final ResponseData searchForSimilar(Set<String> queryWords)
			throws IOException {
		return submitSearch(GOOGLE_SEARCH_URL
				+ URLEncoder.encode(TextAnalyzer.setToString(queryWords), CHARSET));
	}

	public static final ResponseData submitSearch(String searchUrl)
			throws UnsupportedEncodingException, IOException {
		URL url = new URL(searchUrl);

		logger.log(Level.INFO, "search string: " + url.toString());

		Reader reader = new InputStreamReader(url.openStream(), CHARSET);
		GoogleSearchResults results = new Gson()
				.fromJson(reader, GoogleSearchResults.class);

		return results.getResponseData();
	}
}
