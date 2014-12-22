package com.atomrom.ninety7.service.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atomrom.ninety7.service.dao.DigestDao;
import com.atomrom.ninety7.service.dao.Visit;
import com.atomrom.ninety7.service.dao.VisitDao;
import com.atomrom.ninety7.service.search.GoogleSearchResults.ResponseData;
import com.atomrom.ninety7.service.search.GoogleSearchResults.Result;
import com.atomrom.ninety7.service.util.TextAnalyzer;
import com.atomrom.ninety7.service.util.TextUtil;

public class Finder {

	private static final Logger logger = Logger.getLogger(Finder.class
			.getName());

	public static final int MIN_QUERY_WORD_LENGTH = 3;

	public void find() {
		List<Visit> visitedPages = getVisitedPages();

		if (visitedPages.isEmpty()) {
			logger.log(Level.INFO, "No fresh visited page.");
			return;
		}

		for (Visit visit : visitedPages) {
			logger.log(Level.INFO, "visitedPage:" + visit.url);

			Set<String> queryWords = TextUtil.commaSeparatedListToSet(
					visit.metaKeywords, MIN_QUERY_WORD_LENGTH);
			if (queryWords.isEmpty()) {
				queryWords = TextAnalyzer.getKeywords(visit.content);
			}

			try {
				ResponseData searchResults = GoogleSearch
						.searchForSimilar(queryWords);

				List<Result> results = searchResults.getResults();
				if (!results.isEmpty()) {
					int j = 0;
					for (Result result : results) {
						if (++j > 5) {
							break;
						}

						if (DigestDao.doesExist(visit.user, result.getUrl())) {
							logger.log(Level.INFO,
									"Digest exists: " + result.getUrl());

							continue;
						}

						logger.log(Level.INFO, result.getTitle());
						logger.log(Level.INFO, result.getUrl());

						DigestDao.create(visit.user, result.getUrl(), visit.id,
								TextUtil.htmlToText(result.getTitle()),
								getAbstract(result.getUrl(), queryWords),
								TextUtil.setToString(queryWords));
					}
				} else {
					logger.log(Level.INFO, "Nothing has been found.");
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Google search failed for "
						+ queryWords, e);
			}
		}
	}

	private String getAbstract(String pageUrl, Set<String> queryWords) {
		try {
			String abstr = new TextAnalyzer(pageUrl)
					.extractAbstract(queryWords);
			logger.log(Level.INFO, "Abstract extracted from " + pageUrl + ": "
					+ abstr);

			return abstr;
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, "Could not retrieve atricle.", e);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not retrieve atricle.", e);
		}

		return "";
	}

	private List<Visit> getVisitedPages() {
		return VisitDao.findYoungerThan(2 * 60 * 1000);
	}

}
