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

public class Finder {

	private static final Logger logger = Logger.getLogger(Finder.class
			.getName());

	public void find() {
		List<Visit> visitedPages = getVisitedPages();

		for (Visit visit : visitedPages) {
			if (visit == null) {
				logger.log(Level.INFO, "No fresh visited page.");
				return;
			}

			logger.log(Level.INFO, "visitedPage:" + visit.url);

			Set<String> queryWords = TextAnalyzer.getKeywords(visit.content);

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
								result.getTitle(),
								getAbstract(result.getUrl()),
								TextAnalyzer.setToString(queryWords));
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

	private String getAbstract(String pageUrl) {
		try {
			return TextAnalyzer.extractAbstract(pageUrl);
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, "Could not retrieve atricle.", e);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not retrieve atricle.", e);
		}

		return "";
	}

	private List<Visit> getVisitedPages() {
		List<Visit> visits = VisitDao.findYoungerThan(2 * 60 * 1000);

		if (visits.isEmpty()) {
			return null;
		}

		return visits;
	}

}
