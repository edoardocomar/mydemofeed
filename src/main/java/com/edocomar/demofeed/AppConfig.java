package com.edocomar.demofeed;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Configuration data for the application, just a simple wrapper of Properties
 * @author ecomar
 */
public class AppConfig {
	
	private Properties props;

	private static Properties loadProps(String propFilename) throws Exception {
		Properties props = new Properties();
		File propFile = new File(propFilename);
		if(!propFile.isFile() || !propFile.canRead()) {
			throw new IllegalArgumentException("Cannot read from file " + propFilename);
		}
		try ( FileInputStream fis = new FileInputStream(propFile) ) {
			props.load(fis);
		}
		return props;
	}

	
	public AppConfig(String propFilename) throws Exception {
		this(loadProps(propFilename));
	}
	
	private void validate() throws RuntimeException{
		//TODO validate other
		getPort();
		getAvailableFeeds();
	}


	public AppConfig(Properties props) {
		this.props = props;
		validate();
	}
	
	public int getPort() {
		return Integer.parseInt((String)props.getOrDefault("appmain.port", "8080"));
	}
	
	public Set<String> getAvailableFeeds() {
		// TODO cache value
		String csv = (String) props.get("feeds.list");
		if (csv==null) throw new IllegalArgumentException("missing feeds.list property");
		if (csv.trim().isEmpty()) throw new IllegalArgumentException("empty feeds.list property");
		String[] split = csv.split(",");
		if(split.length==0) throw new IllegalArgumentException("empty feeds.list property");
		return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(split))); 
	}
}
