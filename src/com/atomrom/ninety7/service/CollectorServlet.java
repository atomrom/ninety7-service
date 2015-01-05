package com.atomrom.ninety7.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.Visit;
import com.atomrom.ninety7.service.dao.VisitDao;

@SuppressWarnings("serial")
public class CollectorServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(CollectorServlet.class.getName());

	public static final String REQUEST_PARAM_META_KEYWORDS = "metaKeywords";
	public static final String REQUEST_PARAM_META_DESCRIPTION = "metaDescription";

	public static final int MIN_DURATION = 7500; // 7.5 sec

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Visit visit = new Visit(req);
		if (visit.duration > MIN_DURATION) {
			if (visit.metaKeywords == null) {
				logger.log(Level.INFO, "Missing meta keywords on page " + visit.url);
			}

			VisitDao.create(visit);
		} else {
			logger.log(Level.INFO, "Visit " + visit.url + " is not stored; duration=" + visit.duration + " < " + MIN_DURATION);
		}
	}
}
