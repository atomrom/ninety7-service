package com.atomrom.ninety7.service;

import java.io.IOException;
import java.util.List;
import java.util.Random;
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
import com.google.appengine.api.datastore.Text;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class DigestServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(DigestServlet.class
			.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("application/json");
		resp.setHeader("Cache-Control", "no-cache");

		DigestItem digestItem = getDigestItem();
		if (digestItem != null) {
			log.fine(digestItem.toString());

			try {
				JSONObject digestItemJson = new JSONObject();
				digestItemJson.put("url", digestItem.url);
				digestItemJson.put("title", digestItem.title);
				digestItemJson.put("abstract", digestItem.abstr);
				digestItemJson.put("keywords", digestItem.keywords);

				digestItemJson.write(resp.getWriter());

				log.log(Level.INFO, "Json:" + digestItemJson);
			} catch (JSONException e) {
				log.log(Level.SEVERE, "Could not create response Json object.",
						e);
			}
		} else {
			log.log(Level.INFO, "No digest item has been found.");
		}
	}

	private DigestItem getDigestItem() {
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Query query = new Query("DigestItem");
		List<Entity> digestItems = datastore.prepare(query).asList(
				FetchOptions.Builder.withLimit(10));
		if (digestItems.isEmpty()) {
			return null;
		} else {
			int rnd = new Random().nextInt(digestItems.size());
			log.log(Level.INFO, "rnd:" + rnd);

			Entity digestEntity = digestItems.get(rnd);

			return new DigestItem((String) digestEntity.getProperty("url"),
					((Text) digestEntity.getProperty("title")).getValue(),
					((Text) digestEntity.getProperty("abstract")).getValue(),
					(String) digestEntity.getProperty("keywords"));
		}
	}
}
