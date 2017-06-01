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
	 * @return Map of users with their subscribed feeds
	 */
	ConcurrentMap<String, Set<String>> subscriptions();
	
	/**
	 * @return a collection of articles, grouped in {@linkplain FeedArticles} for the given user
	 * @param userFeeds the feeds the user is subscribed to
	 */
	Collection<FeedArticles> articlesFor(String user, Set<String> userFeeds) throws Exception;

	/**
	 * stores the given articles in the given feed
	 */
	void postArticles(String feed, List<Article> articles) throws Exception;
	
	/**
	 * persists the current map of subscriptions
	 * @see #subscriptions()
	 */
	void persistSubscriptions() throws Exception;

	/**
	 * closes any resources
	 */
	void shutdown() throws Exception;
}
