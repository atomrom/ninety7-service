package com.atomrom.ninety7.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.jsoup.Jsoup;

public class TextAnalyzer {

	private static final int MIN_WORD_LENGTH = 3;
	private static final int WORD_COUNT = 5;

	private static final String CHARSET = "UTF-8";

	public static final Set<String> getKeywords(String text) {
		ArrayList<Word> sortedHistogram = getSortedHistogram(text);

		Set<String> queryWords = new TreeSet<String>();

		int maxCount = WORD_COUNT;
		int i = 0;
		for (Word histogramWord : sortedHistogram) {
			if (histogramWord.word.length() > MIN_WORD_LENGTH) {
				queryWords.add(histogramWord.word);
				if (++i >= maxCount) {
					break;
				}
			}
		}

		return queryWords;
	}

	public static ArrayList<Word> getSortedHistogram(String text) {
		HashMap<String, Integer> dictionary = new HashMap<String, Integer>();

		StringTokenizer tokenizer = new StringTokenizer(text);
		while (tokenizer.hasMoreTokens()) {
			String w = tokenizer.nextToken();

			Integer c = dictionary.get(w);
			c = (c == null ? 1 : c.intValue() + 1);

			dictionary.put(w, c);
		}

		ArrayList<Word> sortedHistogram = new ArrayList<Word>(dictionary.size());
		for (Entry<String, Integer> entry : dictionary.entrySet()) {
			sortedHistogram.add(new Word(entry.getKey(), entry.getValue()));
		}
		dictionary = null;

		Collections.sort(sortedHistogram, new Comparator<Word>() {
			@Override
			public int compare(Word o1, Word o2) {
				return (int) Math.signum(o2.count - o1.count);
			}

		});

		return sortedHistogram;
	}

	public static final String extractAbstract(String pageUrl)
			throws IOException {
		String text = "";

		StringBuffer html = new StringBuffer();

		URL url = new URL(pageUrl);
		InputStream inputStream = url.openStream();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, CHARSET));

			String line;
			while ((line = reader.readLine()) != null) {
				html.append(line);
			}
		} finally {
			inputStream.close();
		}

		text = Jsoup.parse(html.toString()).body().text();

		if (text.length() > 280) {
			text = text.substring(0, 280);
		}

		return text;
	}

	public static final String setToString(Set<String> wordSet) {
		StringBuilder sb = new StringBuilder();
		for (String queryWord : wordSet) {
			sb.append(queryWord);
			sb.append(' ');
		}

		return sb.toString().trim();
	}

	static class Word {
		String word;
		int count = 1;

		Word(String word, int count) {
			this.word = word;
			this.count = count;
		}

		@Override
		public String toString() {
			return "Word [word=" + word + ", count=" + count + "]";
		}

	}

}
