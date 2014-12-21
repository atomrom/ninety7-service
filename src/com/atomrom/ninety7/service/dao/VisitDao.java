package com.atomrom.ninety7.service.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class VisitDao {
	private static final Logger logger = Logger.getLogger(VisitDao.class
			.getName());

	public static final String META_KEYWORDS = "metaKeywords";
	public static final String META_DESCRIPTION = "metaDescription";

	public static final void create(Visit visit) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		Entity visitEntity = new Entity("Visit");
		visitEntity.setProperty("user", user);
		visitEntity.setProperty("timestamp", System.currentTimeMillis());
		visitEntity.setProperty("url", visit.url);
		visitEntity.setProperty("visitDuration", visit.duration);
		visitEntity.setProperty("title", new Text(visit.title));
		visitEntity.setProperty("content", new Text(visit.content));
		if (visit.metaKeywords != null) {
			visitEntity
					.setProperty(META_KEYWORDS, new Text(visit.metaKeywords));
		}
		if (visit.metaDescription != null) {
			visitEntity.setProperty(META_DESCRIPTION, new Text(
					visit.metaDescription));
		}

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		datastore.put(visitEntity);
	}

	public static final List<Visit> findYoungerThan(long maxAgeInMillis) {
		List<Visit> rv = new ArrayList<Visit>(15);

		Filter filter = new FilterPredicate("timestamp",
				FilterOperator.GREATER_THAN_OR_EQUAL,
				System.currentTimeMillis() - maxAgeInMillis);

		Query query = new Query("Visit").setFilter(filter);

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		List<Entity> visitedPages = datastore.prepare(query).asList(
				FetchOptions.Builder.withLimit(15));

		for (Entity entity : visitedPages) {
			rv.add(new Visit(entity));
		}

		return rv;
	}

	public void deleteOldItems(long maxAgeInMillis) {
		logger.log(Level.FINE, "deleteOldEntries(" + maxAgeInMillis + ")");

		Filter filter = new FilterPredicate("timestamp",
				FilterOperator.LESS_THAN_OR_EQUAL, System.currentTimeMillis()
						- maxAgeInMillis);

		Query query = new Query("Visit").setFilter(filter);

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		List<Entity> visitedPages = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
		int count = 0;
		for (Entity entity : visitedPages) {
			logger.log(Level.FINE, "delete:" + entity.getKey());

			datastore.delete(entity.getKey());
			++count;
		}

		logger.log(Level.INFO, "Number of items deleted: " + count);
	}

}
