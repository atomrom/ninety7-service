package com.atomrom.ninety7.service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.BanDao;
import com.atomrom.ninety7.service.dao.DigestDao;
import com.atomrom.ninety7.service.dao.ReadDao;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class VoterServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(VoterServlet.class.getName());

	private static final String OPERATION = "operation";

	private static final String BAN = "ban";
	private static final String DEL = "del";
	private static final String READ = "read";

	private static final String URL = "url";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String operation = req.getParameter(OPERATION);
		String url = req.getParameter(URL);

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		logger.log(Level.INFO, "request: " + operation + ", " + url);

		if (BAN.equalsIgnoreCase(operation)) {
			BanDao.create(url);

			DigestDao.deleteByUrl(user, url);
		} else if (DEL.equalsIgnoreCase(operation)) {
			// TODO store read item url

			DigestDao.deleteByUrl(user, url);
		} else if (READ.equalsIgnoreCase(operation)) {
			ReadDao.create(url);

			DigestDao.deleteByUrl(user, url);
		}
	}

}
