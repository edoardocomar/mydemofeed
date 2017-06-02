package com.edocomar.demofeed;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edocomar.demofeed.util.Utils;

/**
 * Configuration data for the application, just a simple wrapper of Properties
 * @author ecomar
 */
public class AppConfig {
	private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);	
	
	private Properties props;
	private Set<String> availableFeeds;

	public AppConfig(String propFilename) throws Exception {
		this(Utils.loadProps(propFilename));
		logger.info(Utils.propsToString(props));
	}
	
	private void validate() throws RuntimeException{
		getPort();
		availableFeeds();
	}


	public AppConfig(Properties props) {
		this.props = props;
		validate();
	}
	
	public int getPort() {
		return Integer.parseInt(props.getProperty("appmain.port", "8080"));
	}
	
	/**
	 * @return an immutable Set with the predefined feeds
	 */
	public Set<String> availableFeeds() {
		// executed during construction, so no need to synch, but in any case it's immutable 
		if(availableFeeds==null) {
			String csv = props.getProperty("predefined.feeds");
			if (csv==null) throw new IllegalArgumentException("missing predefined.feeds property");
			if (csv.trim().isEmpty()) throw new IllegalArgumentException("empty predefined.feeds property");
			String[] split = csv.split(",");
			if(split.length==0) throw new IllegalArgumentException("empty predefined.feeds property");
			availableFeeds = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(split)));
		}
		return availableFeeds;
	}
	
	public String getProperty(String prop, String defaultValue) {
		return props.getProperty(prop, defaultValue);
	}
	
}
