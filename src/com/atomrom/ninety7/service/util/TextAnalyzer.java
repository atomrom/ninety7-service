package com.atomrom.ninety7.service.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class TextAnalyzer {

	private static final Logger logger = Logger.getLogger(TextAnalyzer.class.getName());

	private static final int MIN_WORD_LENGTH = 3;
	private static final int WORD_COUNT = 5;

	public static final int ABSTRACT_MAX_LENGTH = 280;

	private String url;
	private String html;

	private int fullAbstractHashCode;

	public TextAnalyzer(String pageUrl) throws IOException {
		donwloadPage(pageUrl);
	}

	public void donwloadPage(String pageUrl) throws IOException {
		url = pageUrl;

		html = Jsoup.connect(url).get().html();
	}

	public String getHtml() {
		return html;
	}

	public String getText() {
		return Jsoup.parse(html).body().text();
	}

	public int getFullAbstractHashCode() {
		return fullAbstractHashCode;
	}

	public static final Set<String> getKeywords(String text) {
		ArrayList<HistogramEntry> sortedHistogram = getSortedHistogram(text);

		Set<String> queryWords = new TreeSet<String>();

		int maxCount = WORD_COUNT;
		int i = 0;
		for (HistogramEntry histogramWord : sortedHistogram) {
			if (histogramWord.word.length() > MIN_WORD_LENGTH) {
				queryWords.add(histogramWord.word);
				if (++i >= maxCount) {
					break;
				}
			}
		}

		return queryWords;
	}

	public static ArrayList<HistogramEntry> getSortedHistogram(String text) {
		HashMap<String, Integer> dictionary = new HashMap<String, Integer>();

		StringTokenizer tokenizer = new StringTokenizer(text, " \t\n\r\f,.;?!-<>{[(}])/'\"");
		while (tokenizer.hasMoreTokens()) {
			String w = tokenizer.nextToken();

			Integer c = dictionary.get(w);
			c = (c == null ? 1 : c.intValue() + 1);

			dictionary.put(w, c);
		}

		ArrayList<HistogramEntry> sortedHistogram = new ArrayList<HistogramEntry>(dictionary.size());
		for (Entry<String, Integer> entry : dictionary.entrySet()) {
			sortedHistogram.add(new HistogramEntry(entry.getKey(), entry.getValue()));
		}
		dictionary = null;

		Collections.sort(sortedHistogram, new Comparator<HistogramEntry>() {
			@Override
			public int compare(HistogramEntry o1, HistogramEntry o2) {
				return (int) Math.signum(o2.count - o1.count);
			}

		});

		return sortedHistogram;
	}

	public final String extractAbstract(Set<String> queryWords) throws IOException {
		Element element = findMostRelevantElement(queryWords);

		if (element == null) {
			fullAbstractHashCode = 0;
			return "";
		}

		String fullAbstract = element.text();
		fullAbstractHashCode = fullAbstract.hashCode();

		return TextUtil.trimText(fullAbstract, ABSTRACT_MAX_LENGTH);
	}

	public Element findMostRelevantElement(final Set<String> queryWords) {
		Element rootElement = Jsoup.parse(html).body();
		if (rootElement == null) {
			return null;
		}

		logger.log(Level.INFO, "queryWords:" + queryWords.toString());

		RelevantElementFinder relevantElementFinder = new RelevantElementFinder(queryWords);
		rootElement.traverse(relevantElementFinder);

		if (relevantElementFinder.getMostProbablyRelevantElement() != null) {
			return relevantElementFinder.getMostProbablyRelevantElement();
		}

		logger.log(Level.INFO, "Returning root element");

		return rootElement;
	}

	public static class HistogramEntry {
		public String word;
		public int count = 1;

		HistogramEntry(String word, int count) {
			this.word = word;
			this.count = count;
		}

		@Override
		public String toString() {
			return "HistogramEntry [word=" + word + ", count=" + count + "]";
		}

	}

	static class RelevantElementFinder implements NodeVisitor {
		float maxElementRelevanceRank = 0;
		Element mostProbablyRelevantElement;
		Set<String> queryWords;

		RelevantElementFinder(Set<String> queryWords) {
			this.queryWords = queryWords;
		}

		public Element getMostProbablyRelevantElement() {
			return mostProbablyRelevantElement;
		}

		@Override
		public void head(Node node, int depth) {
			if (node instanceof Element) {
				Element element = (Element) node;

				if (!"p".equalsIgnoreCase(element.tagName()) && !"a".equalsIgnoreCase(element.tagName()) && !"strong".equalsIgnoreCase(element.tagName())
						&& !"span".equalsIgnoreCase(element.tagName())) {
					return;
				}

				float strongMultiplier = 1;
				if ("strong".equalsIgnoreCase(element.tagName())) {
					strongMultiplier = 4;
				}

				final float count = TextUtil.countFoundWords(((Element) node).text(), queryWords);
				final float elementRelevanceRank = strongMultiplier * (count * count) / (1 + Math.abs(element.text().length() - ABSTRACT_MAX_LENGTH));

				if (elementRelevanceRank > maxElementRelevanceRank) {
					maxElementRelevanceRank = elementRelevanceRank;
					mostProbablyRelevantElement = element;

					// System.out.println(element.text());
					// System.out.println("r:" + elementRelevanceRank);
					// System.out.println("c:" + count);
					// System.out.println("l:" + element.text().length());
				}
			}
		}

		@Override
		public void tail(Node node, int depth) {
			// empty
		}
	}

	public String getUrl() {
		return url;
	}
}
