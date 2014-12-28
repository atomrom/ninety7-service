package com.atomrom.ninety7.service.dao;

import java.util.logging.Level;
import java.util.logging.Logger;

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

public class ArchiveDao {
	private static final Logger logger = Logger.getLogger(ArchiveDao.class.getName());

	private static final String ENTITY_KIND = "Archive";

	private static final String USER = "user";
	private static final String URL = "url";
	private static final String TIMESTAMP = "timestamp";
	private static final String TITLE = "title";
	private static final String ABSTRACT = "abstract";
	private static final String KEYWORDS = "keywords";
	private static final String FULL_ABSTRACT_HASH_CODE = "fullAbstractHashCode";

	public static final void create(String url, String title, String abstr, String keywords, Integer fullAbstractHashCode) {
		logger.log(Level.INFO, "create(" + url + ", " + title + ", " + abstr + ", " + keywords + ")");

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		Entity entity = new Entity(ENTITY_KIND);
		entity.setProperty(TIMESTAMP, System.currentTimeMillis());
		entity.setProperty(USER, user);
		entity.setProperty(URL, url);
		entity.setProperty(TITLE, title);
		entity.setProperty(ABSTRACT, abstr);
		entity.setProperty(KEYWORDS, keywords);

		entity.setProperty(FULL_ABSTRACT_HASH_CODE, fullAbstractHashCode);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(entity);
	}

	public static boolean isArchived(User user, String url, int fullAbstractHashCode) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate(USER, FilterOperator.EQUAL, user);
		Filter urlFilter = new FilterPredicate(URL, FilterOperator.EQUAL, url);
		Filter fullAbstractHashCodeFilter = new FilterPredicate(FULL_ABSTRACT_HASH_CODE, FilterOperator.EQUAL, fullAbstractHashCode);

		Filter filter = CompositeFilterOperator.and(urlFilter, userFilter, fullAbstractHashCodeFilter);

		Query query = new Query(ENTITY_KIND).setFilter(filter);

		return datastore.prepare(query).countEntities(FetchOptions.Builder.withLimit(1)) > 0;
	}
}
