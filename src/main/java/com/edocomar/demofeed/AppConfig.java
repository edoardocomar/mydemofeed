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
		//TODO validate other values
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
		// TODO cache value
		String csv = props.getProperty("predefined.feeds");
		if (csv==null) throw new IllegalArgumentException("missing predefined.feeds property");
		if (csv.trim().isEmpty()) throw new IllegalArgumentException("empty predefined.feeds property");
		String[] split = csv.split(",");
		if(split.length==0) throw new IllegalArgumentException("empty predefined.feeds property");
		return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(split))); 
	}
	
	public String getProperty(String prop) {
		return props.getProperty(prop);
	}
	
	public String getProperty(String prop, String defaultValue) {
		return props.getProperty(prop, defaultValue);
	}
	
}
