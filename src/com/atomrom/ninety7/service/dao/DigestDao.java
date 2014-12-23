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
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;

public class DigestDao {
	private static final Logger logger = Logger.getLogger(DigestDao.class.getName());

	private static final int MAX_LIST_SIZE = 24;

	private static final String RANK = "rank";

	public static final void create(User user, String url, int visitedPageId, String title, String abstr, String keywords, int rank) {
		Entity digestItem = new Entity("DigestItem");
		digestItem.setProperty("timestamp", System.currentTimeMillis());
		digestItem.setProperty("user", user);
		digestItem.setProperty("url", url);
		digestItem.setProperty("visitedPageId", visitedPageId);
		digestItem.setProperty("title", new Text(title));
		digestItem.setProperty("abstract", new Text(abstr));
		digestItem.setProperty("keywords", keywords);
		digestItem.setProperty(RANK, rank);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(digestItem);
	}

	public static final List<Digest> get(User user, int pageNum) {
		List<Digest> rv = new ArrayList<Digest>(10);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter timestapmFlter = new FilterPredicate(RANK, FilterOperator.EQUAL, pageNum);

		Filter filter = CompositeFilterOperator.and(timestapmFlter, userFilter);

		Query query = new Query("DigestItem").setFilter(filter);

		List<Entity> digestItems = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(MAX_LIST_SIZE));

		for (Entity entity : digestItems) {
			Digest digestItem = new Digest((String) entity.getProperty("url"), ((Text) entity.getProperty("title")).getValue(),
					((Text) entity.getProperty("abstract")).getValue(), (String) entity.getProperty("keywords"), (Long) entity.getProperty(RANK));

			rv.add(digestItem);
		}

		return rv;
	}

	public static boolean doesExist(User user, String url) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter urlFilter = new FilterPredicate("url", FilterOperator.EQUAL, url);

		Filter filter = CompositeFilterOperator.and(urlFilter, userFilter);

		Query query = new Query("DigestItem").setFilter(filter);

		return datastore.prepare(query).countEntities(FetchOptions.Builder.withLimit(1)) > 0;
	}

	public static void deleteByUrl(User user, String url) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter urlFilter = new FilterPredicate("url", FilterOperator.EQUAL, url);

		Filter filter = CompositeFilterOperator.and(urlFilter, userFilter);

		Query query = new Query("DigestItem").setFilter(filter);
		List<Entity> digestsToDelete = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		for (Entity entity : digestsToDelete) {
			logger.log(Level.INFO, "Entity deleted: " + entity.getKey());

			datastore.delete(entity.getKey());
		}
	}

	public void deleteOldEntries(long maxAgeInMillis) {
		logger.log(Level.FINE, "deleteOldEntries(" + maxAgeInMillis + ")");

		Filter filter = new FilterPredicate("timestamp", FilterOperator.LESS_THAN_OR_EQUAL, System.currentTimeMillis() - maxAgeInMillis);

		Query query = new Query("DigestItem").setFilter(filter);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<Entity> visitedPages = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		int count = 0;
		for (Entity entity : visitedPages) {
			logger.log(Level.FINE, "delete:" + entity.getKey());

			datastore.delete(entity.getKey());
			++count;
		}

		logger.log(Level.INFO, "Number of items deleted: " + count);
	}

}
