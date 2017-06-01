package com.edocomar.demofeed;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.edocomar.demofeed.model.Article;

/**
 * In memory context for http testing purposes 
 * @author ecomar
 */
public class AppContextImpl implements AppContext {

	private AppConfig config;
	private ConcurrentMap<String, Set<String>> subscriptions;
	private ConcurrentHashMap<String,List<Article>> articles;

	public AppContextImpl(AppConfig config) {
		this.config = config;
		this.subscriptions = new ConcurrentHashMap<String, Set<String>>();
		
		this.articles = new ConcurrentHashMap<String, List<Article>>();
		for (String feed : config.getAvailableFeeds()) {
			articles.put(feed, Collections.synchronizedList(new LinkedList<Article>()));
		}
	}
	
	@Override
	public AppConfig config() {
		return config;
	}

	@Override
	public Map<String, Set<String>> subscriptions() {
		return subscriptions;
	}

	@Override
	public Set<String> feeds() {
		return config.getAvailableFeeds();
	}

	@Override
	public Map<String, List<Article>> articles() {
		return articles;
	}

}
