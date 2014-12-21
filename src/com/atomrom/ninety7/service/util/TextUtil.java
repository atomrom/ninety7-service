package com.atomrom.ninety7.service.util;

import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.jsoup.Jsoup;

public class TextUtil {

	public static final String META_KEYWORDS_DELIM = ",";

	public static Set<String> commaSeparatedListToSet(String metaKeywords) {
		if (metaKeywords == null) {
			return Collections.<String> emptySet();
		}

		Set<String> rv = new TreeSet<String>();

		StringTokenizer st = new StringTokenizer(metaKeywords,
				META_KEYWORDS_DELIM);
		while (st.hasMoreTokens()) {
			rv.add(st.nextToken());
		}

		return rv;
	}

	public static final String setToString(Set<String> wordSet) {
		StringBuilder sb = new StringBuilder();
		for (String queryWord : wordSet) {
			sb.append(queryWord);
			sb.append(' ');
		}

		return sb.toString().trim();
	}

	public static String htmlToText(String html) {
		if (html == null) {
			return null;
		}

		return Jsoup.parse(html).text();
	}
}
