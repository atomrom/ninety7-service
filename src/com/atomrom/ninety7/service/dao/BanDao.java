package com.atomrom.ninety7.service.dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
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
	public static final String HOST = "host";
	public static final String PATH = "path";

	public static final String CHILDREN_SUFFIX = "*";

	public static enum BanTarget {
		THIS_PAGE, THIS_PAGE_AND_CHILDREN, SITE
	}

	public static final void create(String url, BanTarget banTarget) {
		logger.log(Level.INFO, "create(" + url + ", " + banTarget + ")");

		try {
			URL urlObject = new URL(url);

			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();

			Entity entity = new Entity(ENTITY_KIND);
			entity.setProperty(USER, user);
			entity.setProperty(HOST, urlObject.getHost());

			String path = null;
			switch (banTarget) {
			case THIS_PAGE:
				path = urlObject.getFile();
				break;
			case THIS_PAGE_AND_CHILDREN:
				path = urlObject.getPath() + CHILDREN_SUFFIX;
				break;
			case SITE:
			default:
				path = CHILDREN_SUFFIX;
			}
			entity.setProperty(PATH, path);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(entity);

		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, "Malformed URL: " + url, e);
		}

	}

	public static boolean isBanned(User user, String url) {

		try {
			URL urlObject = new URL(url);

			String host = urlObject.getHost();
			String file = urlObject.getFile();

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

			Filter userFilter = new FilterPredicate(USER, FilterOperator.EQUAL, user);
			Filter urlFilter = new FilterPredicate(HOST, FilterOperator.EQUAL, host);

			Filter filter = CompositeFilterOperator.and(urlFilter, userFilter);

			Query query = new Query(ENTITY_KIND).setFilter(filter);

			Iterable<Entity> entities = datastore.prepare(query).asIterable();

			Ban ban = new Ban();
			for (Entity entity : entities) {
				ban.updateByEntity(entity);
				switch (ban.getTarget()) {
				case THIS_PAGE:
					if (ban.path.equals(file)) {
						return true;
					}
					break;
				case THIS_PAGE_AND_CHILDREN:
					if (file.startsWith(ban.path)) {
						return true;
					}
					break;
				case SITE:				
					return true;
				}
			}

			return false;
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, "Malformed URL: " + url, e);
		}

		return false;
	}
}
