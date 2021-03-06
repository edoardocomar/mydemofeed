package com.edocomar.demofeed.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.edocomar.demofeed.AbstractBackend;
import com.edocomar.demofeed.AppConfig;
import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.FeedArticles;

/**
 * In memory backend for http testing purposes 
 * @author ecomar
 */
public class InMemoryBackend extends AbstractBackend {

	private ConcurrentMap<String,List<Article>> articles;

	public InMemoryBackend(AppConfig config) {
		super(config);
		
		//set of feeds is immutable, prepopulate articles with empty list
		//ensures that articles.get(feed) !=null
		this.articles = new ConcurrentHashMap<String, List<Article>>();
		for (String feed : config.availableFeeds()) {
			articles.put(feed, new LinkedList<Article>());
		}
	}
	
	@Override
	public Collection<FeedArticles> articlesFor(String user, Set<String> userFeeds) {
		List<FeedArticles> result = new ArrayList<>();
		for (String feed : userFeeds) {
			FeedArticles resultFeedArticles = new FeedArticles(feed);
			List<Article> existingArticles = this.articles.get(feed);

			synchronized(existingArticles) {
				if (existingArticles.size()>0) {
					resultFeedArticles.getArticles().addAll(existingArticles);
					result.add(resultFeedArticles);
				}
			}
		}
		return result;
	}

	@Override
	public void postArticles(String feed, List<Article> newArticles) throws Exception {
		List<Article> existingArticles = this.articles.get(feed);
		synchronized (existingArticles) {
			existingArticles.addAll(newArticles);
		}
	}

	@Override
	public void persistSubscriptions() {
		// nothing to do
	}

	@Override
	public void shutdown() {
		// nothing to do
	}
}
