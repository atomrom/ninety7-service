package com.atomrom.ninety7.service;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.DigestDao;

@SuppressWarnings("serial")
public class DigestHousekeeperServlet extends HttpServlet {
	
	private static final long DIGEST_ITEM_MAX_AGE = 2 * 24 * 60 * 60 * 1000; // 2 days
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		DigestDao dao = new DigestDao();
		dao.deleteOldEntries(DIGEST_ITEM_MAX_AGE);
	}

}
