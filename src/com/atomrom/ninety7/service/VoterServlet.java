package com.atomrom.ninety7.service;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.ArchiveDao;
import com.atomrom.ninety7.service.dao.BanDao;
import com.atomrom.ninety7.service.dao.BanDao.BanTarget;
import com.atomrom.ninety7.service.dao.Digest;
import com.atomrom.ninety7.service.dao.DigestDao;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;

@SuppressWarnings("serial")
public class VoterServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(VoterServlet.class.getName());

	private static final String OPERATION = "operation";

	private static final String BAN = "ban";
	private static final String CLOSE = "close";
	private static final String ARCHIVE = "archive";

	private static final String URL = "url";

	private static final String BAN_TARGET = "banTarget";
	private static final String BAN_THIS_PAGE_AND_CHILDREN = "thisPageAndChildren";
	private static final String BAN_SITE = "site";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String operation = req.getParameter(OPERATION);
		String url = req.getParameter(URL);

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		logger.log(Level.INFO, "request: " + operation + ", " + url);

		if (BAN.equalsIgnoreCase(operation)) {
			BanTarget banTarget = BanTarget.THIS_PAGE;

			String banTargetInRequest = req.getParameter(BAN_TARGET);
			if (banTargetInRequest != null) {
				if (BAN_THIS_PAGE_AND_CHILDREN.equals(banTargetInRequest)) {
					banTarget = BanTarget.THIS_PAGE_AND_CHILDREN;
				} else if (BAN_SITE.equals(banTargetInRequest)) {
					banTarget = BanTarget.SITE;
				} 
				
				logger.log(Level.INFO, "Ban target: '" + banTargetInRequest + "' --> " + banTarget);
			}

			BanDao.create(url, banTarget);

			List<String> deletedItems = DigestDao.deleteByBan(user, url, banTarget);

			JSONArray deletedItemsArray = new JSONArray(deletedItems);
			try {
				resp.setContentType("application/json; charset=utf-8");
				resp.setHeader("Cache-Control", "no-cache");
				
				deletedItemsArray.write(resp.getWriter());
			} catch (JSONException e) {
				logger.log(Level.SEVERE, "Could not create response Json object.", e);
			}
		} else if (CLOSE.equalsIgnoreCase(operation)) {
			DigestDao.setClosed(user, url);
		} else if (ARCHIVE.equalsIgnoreCase(operation)) {
			Digest digest = DigestDao.setClosed(user, url);

			if (digest != null) {
				ArchiveDao.create(url, digest.title, digest.abstr, digest.keywords, digest.fullAbstractHashCode);
			}
		}
	}

}
