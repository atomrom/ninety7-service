package com.atomrom.ninety7.service;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.VisitDao;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

@SuppressWarnings("serial")
public class StatisticsServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(StatisticsServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		final String statType = (String) req.getParameter("q");

		logger.log(Level.INFO, statType);

		StringBuilder keywords = new StringBuilder();

		Query query = new Query("Visit");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> visitedPages = datastore.prepare(query).asIterable();

		HashMap<String, Interest> keywordMap = new HashMap<String, Interest>();

		for (Entity entity : visitedPages) {
			String keyword = ((Text) entity.getProperty(VisitDao.META_KEYWORDS)).getValue();

			logger.log(Level.INFO, (String) entity.getProperty(VisitDao.URL) + ";" + keyword);

			StringTokenizer tokenizer = new StringTokenizer(keyword, " \t\n\r\f,.;?!-<>{[(}])/'\"");
			while (tokenizer.hasMoreTokens()) {
				String w = tokenizer.nextToken();

				Interest interest = keywordMap.get(w);
				if (interest == null) {
					interest = new Interest();

					keywordMap.put(w, interest);
				}
				interest.increase((Long) entity.getProperty(VisitDao.VISIT_DURATION));
			}

			if ("wpc".equals(statType)) {
				List<String> pairs = getPairs(keyword);
				for (String w : pairs) {
					Interest interest = keywordMap.get(w);
					if (interest == null) {
						interest = new Interest();

						keywordMap.put(w, interest);
					}
					interest.increase((Long) entity.getProperty(VisitDao.VISIT_DURATION));
				}
			}
		}

		ArrayList<HistogramEntry> sortedHistogram = new ArrayList<HistogramEntry>(keywordMap.size());
		for (Entry<String, Interest> entry : keywordMap.entrySet()) {
			sortedHistogram.add(new HistogramEntry(entry.getKey(), entry.getValue()));
		}
		keywordMap = null;

		Collections.sort(sortedHistogram, new Comparator<HistogramEntry>() {
			@Override
			public int compare(HistogramEntry o1, HistogramEntry o2) {
				if ("c".equals(statType) || "wpc".equals(statType)) {
					return (int) Math.signum(o2.count - o1.count);
				}
				if ("d".equals(statType)) {
					return (int) Math.signum(o2.duration - o1.duration);
				}
				if ("md".equals(statType)) {
					return (int) Math.signum(o2.maxDuration - o1.maxDuration);
				}
				if ("ad".equals(statType)) {
					return (int) Math.signum(o2.avgDuration - o1.avgDuration);
				}

				return 0;
			}

		});

		logger.log(Level.INFO, keywords.toString());

		resp.setHeader("Cache-Control", "no-cache");
		resp.setContentType("text/plain; charset=utf-8");

		Writer writer = resp.getWriter();

		for (HistogramEntry entry : sortedHistogram) {
			writer.append(entry.word);
			writer.append(',');
			if ("c".equals(statType) || "wpc".equals(statType)) {
				writer.append(Integer.toString(entry.count));
			}
			if ("d".equals(statType)) {
				writer.append(Long.toString(entry.duration));
			}
			if ("md".equals(statType)) {
				writer.append(Long.toString(entry.maxDuration));
			}
			if ("ad".equals(statType)) {
				writer.append(Long.toString(entry.avgDuration));
			}

			writer.append('\n');
		}
	}

	static class Interest {
		int count = 0;
		long duration = 0;
		long maxDuration = 0;
		long avgDuration = 0;

		public Interest(int count, long duration, long maxDuration) {
			this.count = count;
			this.duration = duration;
			this.maxDuration = maxDuration;

			avgDuration = duration / count;
		}

		public void increase(long duration) {
			this.duration += duration;
			this.maxDuration = Math.max(this.maxDuration, duration);

			count++;

			avgDuration = duration / count;
		}

		public Interest() {
			// empty
		}
	}

	static class HistogramEntry extends Interest {
		String word;

		public HistogramEntry(String word, Interest interest) {
			super(interest.count, interest.duration, interest.maxDuration);

			this.word = word;
		}
	}

	private List<String> getPairs(String keywords) {
		List<String> words = new ArrayList<String>(10);

		StringTokenizer tokenizer = new StringTokenizer(keywords, " \t\n\r\f,.;?!-<>{[(}])/'\"");
		while (tokenizer.hasMoreTokens()) {
			String w = tokenizer.nextToken();
			words.add(w);
		}

		List<String> pairs = new ArrayList<String>((words.size() * (words.size() - 1)) / 2);

		for (int i = 0; i < words.size(); ++i) {
			String w1 = words.get(i);

			for (int j = i + 1; j < words.size(); ++j) {
				String w2 = words.get(j);
				pairs.add(w1.compareToIgnoreCase(w2) > 0 ? w1 + " & " + w2 : w2 + " & " + w1);
			}
		}

		return pairs;
	}

}
