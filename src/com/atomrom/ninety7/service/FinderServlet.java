package com.atomrom.ninety7.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;

import com.atomrom.ninety7.service.GoogleResults.ResponseData;
import com.atomrom.ninety7.service.GoogleResults.Result;
import com.atomrom.ninety7.service.dao.VisitedPage;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class FinderServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(FinderServlet.class
			.getName());

	private static Random random = new Random();

	private static final String GOOGLE_SEARCH_URL = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	private static final String CHARSET = "UTF-8";

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		VisitedPage visitedPage = getVisitedPage();
		if (visitedPage != null) {
			HashMap<String, Integer> dictionary = new HashMap<String, Integer>();

			StringTokenizer tokenizer = new StringTokenizer(visitedPage.content);
			while (tokenizer.hasMoreTokens()) {
				String w = tokenizer.nextToken();

				Integer c = dictionary.get(w);
				c = (c == null ? 1 : c.intValue() + 1);
				
				dictionary.put(w, c);
			}

			ArrayList<Word> sortedHistogram = new ArrayList<Word>(
					dictionary.size());
			for (Entry<String, Integer> entry : dictionary.entrySet()) {
				sortedHistogram.add(new Word(entry.getKey(), entry.getValue()));
			}
			dictionary = null;

			Collections.sort(sortedHistogram, new WordComparator());
			Set<String> queryWords = new TreeSet<String>();

			int maxCount = 5;
			int i = 0;
			for (Word histogramWord : sortedHistogram) {
				if (histogramWord.word.length() > 3) {
					queryWords.add(histogramWord.word);
					if (++i >= maxCount) {
						break;
					}
				}
			}

			log.log(Level.INFO, sortedHistogram.toString());
			log.log(Level.INFO, "QUERY: " + queryWords.toString());

			ResponseData searchResults = searchForSimilar(queryWords);
			List<Result> results = searchResults.getResults();

			if (!results.isEmpty()) {
				Result result = results.get(0);

				log.log(Level.INFO, result.getTitle());
				log.log(Level.INFO, result.getUrl());

				Entity digestItem = new Entity("DigestItem");
				digestItem.setProperty("timestamp", System.currentTimeMillis());
				digestItem.setProperty("url", result.getUrl());
				digestItem.setProperty("visitedPageId", visitedPage.id);
				digestItem.setProperty("title", new Text(result.getTitle()));
				digestItem.setProperty("abstract",
						new Text(getAbstract(result.getUrl())));

				DatastoreService datastore = DatastoreServiceFactory
						.getDatastoreService();
				datastore.put(digestItem);
			} else {
				log.log(Level.INFO, "Nothing has been found.");
			}
		}

	}

	private String getAbstract(String pageUrl) {
		String text = "";

		try {
			StringBuffer html = new StringBuffer();

			URL url = new URL(pageUrl);
			InputStream inputStream = url.openStream();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, CHARSET));

				String line;
				while ((line = reader.readLine()) != null) {
					html.append(line);
				}
			} finally {
				inputStream.close();
			}

			text = Jsoup.parse(html.toString()).body().text();

			log.log(Level.INFO, "found text:" + text);

			if (text.length() > 280) {
				text = text.substring(0, 280);
			}
		} catch (MalformedURLException e) {
			log.log(Level.WARNING, "Could not retrieve atricle.", e);
		} catch (IOException e) {
			log.log(Level.WARNING, "Could not retrieve atricle.", e);
		}

		return text;
	}

	private ResponseData searchForSimilar(Set<String> queryWords)
			throws IOException {
		StringBuilder search = new StringBuilder();
		for (String queryWord : queryWords) {
			search.append(queryWord);
			search.append(' ');
		}

		

		return submitSearch(GOOGLE_SEARCH_URL
				+ URLEncoder.encode(search.toString(), CHARSET));
	}

	private ResponseData submitSearch(String searchUrl)
			throws UnsupportedEncodingException, IOException {
		URL url = new URL(searchUrl);

		log.log(Level.INFO, "search string: " + url.toString());

		Reader reader = new InputStreamReader(url.openStream(), CHARSET);
		GoogleResults results = new Gson()
				.fromJson(reader, GoogleResults.class);

		return results.getResponseData();
	}

	private VisitedPage getVisitedPage() {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("Visit");
		List<Entity> visitedPages = datastore.prepare(query).asList(
				FetchOptions.Builder.withLimit(15));
		if (visitedPages.isEmpty()) {
			return null;
		} else {
			int rnd = random.nextInt(visitedPages.size());
			Entity pageEntity = visitedPages.get(rnd);

			return new VisitedPage((Integer) pageEntity.getProperty("id"),
					(String) pageEntity.getProperty("url"),
					((Text) pageEntity.getProperty("title")).getValue(),
					((Text) pageEntity.getProperty("content")).getValue());
		}
	}

	class Word {
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

	class WordComparator implements Comparator<Word> {
		@Override
		public int compare(Word o1, Word o2) {
			return (int) Math.signum(o2.count - o1.count);
		}

	}

}
