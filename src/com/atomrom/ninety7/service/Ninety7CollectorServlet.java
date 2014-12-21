package com.atomrom.ninety7.service;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.Visit;
import com.atomrom.ninety7.service.dao.VisitDao;

@SuppressWarnings("serial")
public class Ninety7CollectorServlet extends HttpServlet {
	public static final String REQUEST_PARAM_META_KEYWORDS = "metaKeywords";
	public static final String REQUEST_PARAM_META_DESCRIPTION = "metaDescription";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		VisitDao.create(new Visit(req));
	}
}
