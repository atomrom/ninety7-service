package com.atomrom.ninety7.service.dao;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atomrom.ninety7.service.VoterServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class BanDao {
	private static final Logger logger = Logger.getLogger(BanDao.class.getName());

	private static final String ENTITY_KIND = "Banned";

	private static final String USER = "user";
	private static final String URL = "url";

	public static final void create(String url) {
		logger.log(Level.INFO, "create(" + url + ")");

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		Entity entity = new Entity(ENTITY_KIND);
		entity.setProperty(USER, user);
		entity.setProperty(URL, url);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(entity);
	}

	public static boolean isBanned(User user, String url) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter urlFilter = new FilterPredicate("url", FilterOperator.EQUAL, url);

		Filter filter = CompositeFilterOperator.and(urlFilter, userFilter);

		Query query = new Query(ENTITY_KIND).setFilter(filter);

		return datastore.prepare(query).countEntities(FetchOptions.Builder.withLimit(1)) > 0;
	}
}
