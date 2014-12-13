package com.atomrom.ninety7.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.dao.DigestItem;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class DigestServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(DigestServlet.class
			.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		UserService userService = UserServiceFactory.getUserService();
		User currentUser = userService.getCurrentUser();

		resp.setContentType("application/json");
		resp.setHeader("Cache-Control", "no-cache");

		if (currentUser == null) {
			logger.log(Level.INFO, "User is not logged in.");
			return;
		}
		logger.log(Level.INFO,
				"request from user: " + currentUser);

		JSONArray digestItemArray = new JSONArray();

		List<DigestItem> digestItems = getDigestItems(currentUser);
		for (DigestItem digestItem : digestItems) {
			logger.fine(digestItem.toString());

			try {
				JSONObject digestItemJson = new JSONObject();

				digestItemJson.put("url", digestItem.url);
				digestItemJson.put("title", digestItem.title);
				digestItemJson.put("abstract", digestItem.abstr);
				digestItemJson.put("keywords", digestItem.keywords);

				digestItemArray.put(digestItemJson);

				logger.log(Level.INFO, "Json:" + digestItemJson);
			} catch (JSONException e) {
				logger.log(Level.SEVERE,
						"Could not create response Json object.", e);
			}

		}

		if (!digestItems.isEmpty()) {
			try {
				logger.log(Level.INFO, "Json:" + digestItemArray);

				digestItemArray.write(resp.getWriter());
			} catch (JSONException e) {
				logger.log(Level.SEVERE,
						"Could not create response Json object.", e);
			}
		} else {
			logger.log(Level.INFO, "No digest item has been found.");
		}
	}

	private List<DigestItem> getDigestItems(User user) {
		List<DigestItem> rv = new ArrayList<DigestItem>(10);

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("DigestItem").setFilter(new FilterPredicate(
				"user", FilterOperator.EQUAL, user));
		List<Entity> digestItems = datastore.prepare(query).asList(
				FetchOptions.Builder.withLimit(12));

		for (Entity entity : digestItems) {
			DigestItem digestItem = new DigestItem(
					(String) entity.getProperty("url"),
					((Text) entity.getProperty("title")).getValue(),
					((Text) entity.getProperty("abstract")).getValue(),
					(String) entity.getProperty("keywords"));

			rv.add(digestItem);
		}

		return rv;
	}
}
