package com.atomrom.ninety7.service;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.VisitDao;

@SuppressWarnings("serial")
public class VisitHousekeeperServlet extends HttpServlet {

	private static final long VISIT_MAX_AGE = 3 * 24 * 60 * 60 * 1000; // 3 days

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		VisitDao dao = new VisitDao();
		dao.deleteOldItems(VISIT_MAX_AGE);
	}

}
