package com.atomrom.ninety7.service.dao;

import com.atomrom.ninety7.service.dao.BanDao.BanTarget;
import com.google.appengine.api.datastore.Entity;

public class Ban {

	public String path;
	public String host;

	public BanTarget banTarget;

	public void updateByEntity(Entity entity) {
		host = (String) entity.getProperty(BanDao.HOST);
		path = (String) entity.getProperty(BanDao.PATH);

		if (BanDao.CHILDREN_SUFFIX.equals(path)) {
			path = "";
			banTarget = BanTarget.SITE;
		} else if (path != null && path.endsWith(BanDao.CHILDREN_SUFFIX)) {
			path = path.substring(0, path.length() - BanDao.CHILDREN_SUFFIX.length());
			banTarget = BanTarget.THIS_PAGE_AND_CHILDREN;
		} else {
			banTarget = BanTarget.THIS_PAGE;
		}
	}

	public BanTarget getTarget() {
		return banTarget;
	}

}
