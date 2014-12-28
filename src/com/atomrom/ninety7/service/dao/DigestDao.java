package com.atomrom.ninety7.service.dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atomrom.ninety7.service.dao.BanDao.BanTarget;
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

	private static final String ENTITY_KIND = "DigestItem";

	private static final String USER = "user";
	private static final String URL = "url";
	private static final String RANK = "rank";
	private static final String FULL_ABSTRACT_HASH_CODE = "fullAbstractHashCode";
	private static final String CLOSED = "closed";

	public static enum DoesExistResponse {
		YES, NO, WITH_DIFFERENT_ABSTRACT
	};

	public static final void create(User user, String url, int visitedPageId, String title, String abstr, String keywords, int rank, int fullAbstractHashCode) {
		Entity digestItem = new Entity(ENTITY_KIND);
		digestItem.setProperty("timestamp", System.currentTimeMillis());
		digestItem.setProperty(USER, user);
		digestItem.setProperty(URL, url);
		digestItem.setProperty("visitedPageId", visitedPageId);
		digestItem.setProperty("title", new Text(title));
		digestItem.setProperty("abstract", new Text(abstr));
		digestItem.setProperty("keywords", keywords);
		digestItem.setProperty(RANK, rank);
		digestItem.setProperty(FULL_ABSTRACT_HASH_CODE, fullAbstractHashCode);
		digestItem.setProperty(CLOSED, false);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(digestItem);
	}

	public static final void update(User user, String url, int visitedPageId, String title, String abstr, String keywords, int rank, int fullAbstractHashCode) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate(USER, FilterOperator.EQUAL, user);
		Filter rankFilter = new FilterPredicate(URL, FilterOperator.EQUAL, url);

		Filter filter = CompositeFilterOperator.and(rankFilter, userFilter);

		Query query = new Query(ENTITY_KIND).setFilter(filter);
		List<Entity> digestItems = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));

		Entity digestItem;
		if (digestItems.isEmpty()) {
			digestItem = new Entity(ENTITY_KIND);

			digestItem.setProperty(USER, user);
			digestItem.setProperty(URL, url);
		} else {
			digestItem = digestItems.get(0);
		}

		digestItem.setProperty("timestamp", System.currentTimeMillis());
		digestItem.setProperty("visitedPageId", visitedPageId);
		digestItem.setProperty("title", new Text(title));
		digestItem.setProperty("abstract", new Text(abstr));
		digestItem.setProperty("keywords", keywords);
		digestItem.setProperty(RANK, rank);
		digestItem.setProperty(FULL_ABSTRACT_HASH_CODE, fullAbstractHashCode);

		digestItem.setProperty(CLOSED, false);

		datastore.put(digestItem);
	}

	public static final List<Digest> getOpenDigests(User user, int pageNum) {
		List<Digest> rv = new ArrayList<Digest>(10);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter timestapmFilter = new FilterPredicate(RANK, FilterOperator.EQUAL, pageNum);
		Filter closedFilter = new FilterPredicate(CLOSED, FilterOperator.EQUAL, false);

		Filter filter = CompositeFilterOperator.and(timestapmFilter, userFilter, closedFilter);

		Query query = new Query("DigestItem").setFilter(filter);

		List<Entity> digestItems = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(MAX_LIST_SIZE));

		for (Entity entity : digestItems) {
			rv.add(entityToDigest(entity));
		}

		return rv;
	}

	public static DoesExistResponse doesExist(User user, String url, int fullAbstractHashCode) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter urlFilter = new FilterPredicate("url", FilterOperator.EQUAL, url);

		Filter filter = CompositeFilterOperator.and(urlFilter, userFilter);

		Query query = new Query("DigestItem").setFilter(filter);
		List<Entity> digestItems = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(MAX_LIST_SIZE));

		for (Entity entity : digestItems) {
			Long hashCode = (Long) entity.getProperty(FULL_ABSTRACT_HASH_CODE);
			if (hashCode != null) {
				if (fullAbstractHashCode != hashCode) {
					return DoesExistResponse.WITH_DIFFERENT_ABSTRACT;
				}
			}
		}

		return digestItems.isEmpty() ? DoesExistResponse.NO : DoesExistResponse.YES;
	}

	public static Digest setClosed(User user, String url) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		List<Entity> digestsToClose = getMatchingEntities(datastore, user, url);

		for (Entity entity : digestsToClose) {
			logger.log(Level.INFO, "Entity closed: " + entity.getKey());

			entity.setProperty(CLOSED, true);
			datastore.put(entity);
		}

		if (!digestsToClose.isEmpty()) {
			return entityToDigest(digestsToClose.get(0));
		} else {
			return null;
		}
	}

	public static void deleteByUrl(User user, String url) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		List<Entity> digestsToDelete = getMatchingEntities(datastore, user, url);

		for (Entity entity : digestsToDelete) {
			logger.log(Level.INFO, "Entity deleted: " + entity.getKey());

			datastore.delete(entity.getKey());
		}
	}

	public static List<String> deleteItemsByUrlPrefix(User user, String urlPrefix) {
		logger.log(Level.INFO, "deleteItemsByUrlPrefix(" + user + "," + urlPrefix + ")");

		List<String> deletedItemUrls = new ArrayList<String>();

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter urlFilterLowerBound = new FilterPredicate(URL, FilterOperator.GREATER_THAN_OR_EQUAL, urlPrefix);
		Filter urlFilterUpperBound = new FilterPredicate(URL, FilterOperator.LESS_THAN_OR_EQUAL, urlPrefix + "Z");

		Filter filter = CompositeFilterOperator.and(userFilter, urlFilterLowerBound, urlFilterUpperBound);

		Query query = new Query(ENTITY_KIND).setFilter(filter);
		List<Entity> digestsToDelete = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		if (digestsToDelete.isEmpty()) {
			logger.log(Level.INFO, "Nothing to delete by Url prefix");
		}

		for (Entity entity : digestsToDelete) {
			deletedItemUrls.add((String) entity.getProperty(URL));

			logger.log(Level.INFO, "Entity deleted: " + entity.getKey() + ", " + entity.getProperty(URL));
			datastore.delete(entity.getKey());
		}

		return deletedItemUrls;
	}

	private static List<Entity> getMatchingEntities(DatastoreService datastore, User user, String url) {
		Filter userFilter = new FilterPredicate("user", FilterOperator.EQUAL, user);
		Filter urlFilter = new FilterPredicate("url", FilterOperator.EQUAL, url);

		Filter filter = CompositeFilterOperator.and(urlFilter, userFilter);

		Query query = new Query("DigestItem").setFilter(filter);
		List<Entity> matchingEntities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		return matchingEntities;
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

	public static Digest get(User user, String url) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate(USER, FilterOperator.EQUAL, user);
		Filter urlFilter = new FilterPredicate(URL, FilterOperator.EQUAL, url);

		Filter filter = CompositeFilterOperator.and(urlFilter, userFilter);

		Query query = new Query(ENTITY_KIND).setFilter(filter);

		List<Entity> digestItems = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1));

		for (Entity entity : digestItems) {
			return entityToDigest(entity);
		}

		return null;
	}

	private static Digest entityToDigest(Entity entity) {
		Long hashCode = (Long) entity.getProperty(FULL_ABSTRACT_HASH_CODE);
		Integer fullAbstractHashCode = null;
		if (hashCode != null) {
			fullAbstractHashCode = (int) hashCode.longValue();
		}

		return new Digest((String) entity.getProperty("url"), ((Text) entity.getProperty("title")).getValue(),
				((Text) entity.getProperty("abstract")).getValue(), (String) entity.getProperty("keywords"), (Long) entity.getProperty(RANK),
				fullAbstractHashCode);
	}

	public static List<String> deleteByBan(User user, String url, BanTarget banTarget) {
		logger.log(Level.INFO, "deleteByBan(" + user + "," + url + "," + banTarget + ")");

		List<String> deletedDigests = Collections.emptyList();

		switch (banTarget) {
		case THIS_PAGE:
			deleteByUrl(user, url);

			deletedDigests = new ArrayList<String>();
			deletedDigests.add(url);
			break;
		case THIS_PAGE_AND_CHILDREN:
			deletedDigests = deleteItemsByUrlPrefix(user, url);
			break;
		case SITE:

			try {
				URL urlObject = new URL(url);
				urlObject.getHost();
				deletedDigests = deleteItemsByUrlPrefix(user, urlObject.getProtocol() + "://" + urlObject.getHost());
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, "Malformed url: " + url, e);
			}

			break;
		}

		return deletedDigests;
	}

}
