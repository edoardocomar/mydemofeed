package com.edocomar.demofeed;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.edocomar.demofeed.model.Article;

/**
 * Object containing Application context common to multiple resources 
 * @author ecomar
 *
 */
public interface AppContext {

	AppConfig config();
	
	/**
	 * @return Map of users to their subscribed feeds
	 */
	Map<String, Set<String>> subscriptions();

	Set<String> feeds();

	/**
	 * @return Map of feed to their posted articles
	 */
	Map<String, List<Article>> articles();
}
