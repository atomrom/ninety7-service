package com.atomrom.ninety7.service.util;

import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.jsoup.Jsoup;

public class TextUtil {

	public static final String META_KEYWORDS_DELIM = ", ";

	public static final String TRIMMED_TEXT_SUFFIX = "...";

	public static Set<String> commaSeparatedListToSet(String metaKeywords,
			int minWordLength) {
		if (metaKeywords == null) {
			return Collections.<String> emptySet();
		}

		Set<String> rv = new TreeSet<String>();

		StringTokenizer st = new StringTokenizer(metaKeywords,
				META_KEYWORDS_DELIM);
		while (st.hasMoreTokens()) {
			String word = st.nextToken();

			if (minWordLength <= word.length()) {
				rv.add(word);
			}
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

	public static int countFoundWords(String text, Set<String> queryWords) {
		int count = 0;

		for (String qw : queryWords) {
			if (text.toUpperCase().contains(qw.toUpperCase())) {
				++count;
			}
		}

		return count;
	}

	public static String trimText(String text, int maxLength) {
		String rv;

		if (text.length() > maxLength) {
			rv = text.substring(0, maxLength);
			rv += TRIMMED_TEXT_SUFFIX;
		} else {
			rv = text;
		}

		return rv;
	}
}
