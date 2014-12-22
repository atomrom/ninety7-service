package com.atomrom.ninety7.service;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.DigestDao;
import com.atomrom.ninety7.service.dao.Digest;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;

@SuppressWarnings("serial")
public class DigestServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(DigestServlet.class
			.getName());

	private static final long DAY_IN_MILLIS = 86400000l;

	private String PAGE_NUM = "pageNum";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		UserService userService = UserServiceFactory.getUserService();
		User currentUser = userService.getCurrentUser();

		int pageNum = Integer.parseInt(req.getParameter(PAGE_NUM));

		resp.setContentType("application/json; charset=utf-8");
		resp.setHeader("Cache-Control", "no-cache");

		if (currentUser == null) {
			logger.log(Level.INFO, "User is not logged in.");
			return;
		}
		logger.log(Level.INFO, "Request from user: " + currentUser
				+ ", pageNum: " + pageNum);

		JSONArray digestItemArray = new JSONArray();

		List<Digest> digestItems = DigestDao.get(currentUser, pageNum);
		for (Digest digestItem : digestItems) {
			logger.log(Level.INFO, digestItem.toString());

			try {
				digestItemArray.put(digestItem.toJson());
			} catch (JSONException e) {
				logger.log(Level.SEVERE,
						"Could not create response Json object.", e);
			}
		}

		if (!digestItems.isEmpty()) {
			try {
				digestItemArray.write(resp.getWriter());
			} catch (JSONException e) {
				logger.log(Level.SEVERE,
						"Could not create response Json object.", e);
			}
		} else {
			logger.log(Level.INFO, "No digest item has been found.");
		}
	}

}
