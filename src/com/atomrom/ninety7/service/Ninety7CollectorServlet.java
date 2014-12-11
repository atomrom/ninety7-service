package com.atomrom.ninety7.service;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

@SuppressWarnings("serial")
public class Ninety7CollectorServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// UserService userService = UserServiceFactory.getUserService();
		// User user = userService.getCurrentUser();

		String urlVisited = req.getParameter("urlVisited");
		String visitDuration = req.getParameter("visitDuration");
		String title = req.getParameter("title");
		String content = req.getParameter("content");
				
		Entity visit = new Entity("Visit");
		visit.setProperty("timestamp",System.currentTimeMillis());
		visit.setProperty("url", urlVisited);
		visit.setProperty("visitDuration", visitDuration);
		visit.setProperty("title", new Text(title));
		visit.setProperty("content", new Text(content));
		
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		datastore.put(visit);
	}
}
