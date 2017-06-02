package com.edocomar.demofeed;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.FeedArticles;

/**
 * Common application context used by multiple JAX-RS resources 
 * @author ecomar
 */
public interface AppBackend {

	AppConfig config();

	/**
	 * @return Map of users to their subscribed feeds
	 */
	ConcurrentMap<String, Set<String>> subscriptions();

	Collection<FeedArticles> articlesFor(String user, Set<String> userFeeds) throws Exception;

	void postArticles(String feed, List<Article> articles) throws Exception;

	void persistSubscriptions() throws Exception;
}
