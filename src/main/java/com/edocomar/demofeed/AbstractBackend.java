package com.edocomar.demofeed;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractBackend implements AppBackend {

	private AppConfig config;
	private ConcurrentMap<String, Set<String>> subscriptions;

	public AbstractBackend(AppConfig config) {
		this.config = config;
		this.subscriptions = new ConcurrentHashMap<String, Set<String>>();
	}

	@Override
	public AppConfig config() {
		return config;
	}

	@Override
	public ConcurrentMap<String, Set<String>> subscriptions() {
		return subscriptions;
	}


}
