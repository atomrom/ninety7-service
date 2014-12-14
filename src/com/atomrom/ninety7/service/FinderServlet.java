package com.atomrom.ninety7.service;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomrom.ninety7.service.search.Finder;

@SuppressWarnings("serial")
public class FinderServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		Finder finder = new Finder();
		finder.find();
	}

}
