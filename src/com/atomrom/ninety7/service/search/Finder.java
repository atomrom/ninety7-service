package com.atomrom.ninety7.service.search;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atomrom.ninety7.service.dao.ArchiveDao;
import com.atomrom.ninety7.service.dao.BanDao;
import com.atomrom.ninety7.service.dao.DigestDao;
import com.atomrom.ninety7.service.dao.Visit;
import com.atomrom.ninety7.service.dao.VisitDao;
import com.atomrom.ninety7.service.search.GoogleSearchResults.ResponseData;
import com.atomrom.ninety7.service.search.GoogleSearchResults.Result;
import com.atomrom.ninety7.service.util.TextAnalyzer;
import com.atomrom.ninety7.service.util.TextUtil;

public class Finder {

	private static final Logger logger = Logger.getLogger(Finder.class.getName());

	public static final int MIN_QUERY_WORD_LENGTH = 3;

	public void find() {
		List<Visit> visitedPages = getVisitedPages();

		if (visitedPages.isEmpty()) {
			logger.log(Level.INFO, "No fresh visited page.");
			return;
		}

		for (Visit visit : visitedPages) {
			logger.log(Level.INFO, "visitedPage:" + visit.url);

			Set<String> queryWords = TextUtil.commaSeparatedListToSet(visit.metaKeywords, MIN_QUERY_WORD_LENGTH);
			if (queryWords.isEmpty()) {
				queryWords = TextUtil.commaSeparatedListToSet(visit.extractedKeywords, MIN_QUERY_WORD_LENGTH);
				if (queryWords.isEmpty()) {
					continue;
				}
			}

			try {
				ResponseData searchResults = GoogleSearch.searchForSimilar(queryWords);

				if (searchResults == null) {
					logger.log(Level.INFO, "Empty results.");
					continue;
				}

				List<Result> results = searchResults.getResults();
				if (results.isEmpty()) {
					logger.log(Level.INFO, "Nothing has been found.");
					continue;
				}

				int j = 0;
				for (Result result : results) {
					if (BanDao.isBanned(visit.user, result.getUrl())) {
						logger.log(Level.INFO, "Page banned: " + result.getUrl());

						continue;
					}

					TextAnalyzer textAnalyzer = new TextAnalyzer(result.getUrl());
					String abstr = textAnalyzer.extractAbstract(queryWords);
					final int fullAbstractHashCode = textAnalyzer.getFullAbstractHashCode();

					if (ArchiveDao.isArchived(visit.user, result.getUrl(), fullAbstractHashCode)) {
						logger.log(Level.INFO, "Digest already archived: " + result.getUrl());
						continue;
					}

					switch (DigestDao.doesExist(visit.user, result.getUrl(), fullAbstractHashCode)) {
					case YES:
						logger.log(Level.INFO, "Digest exists: " + result.getUrl());
						continue;
					case NO:
						DigestDao.create(visit.user, result.getUrl(), visit.id, TextUtil.htmlToText(result.getTitle()), abstr,
								TextUtil.setToString(queryWords), j, fullAbstractHashCode);
						logger.log(Level.INFO, "New digest: " + result.getTitle() + ", " + result.getUrl());
						break;
					case WITH_DIFFERENT_ABSTRACT:
						DigestDao.update(visit.user, result.getUrl(), visit.id, TextUtil.htmlToText(result.getTitle()), abstr,
								TextUtil.setToString(queryWords), j, fullAbstractHashCode);
						logger.log(Level.INFO, "Digest updated: " + result.getTitle() + ", " + result.getUrl());
						break;
					}

					if (++j > 5) {
						break;
					}
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Google search failed for " + queryWords, e);
			}
		}
	}

	private List<Visit> getVisitedPages() {
		return VisitDao.findYoungerThan(2 * 60 * 1000);
	}

}
