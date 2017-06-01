package com.edocomar.demofeed;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

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
	
	public AppConfig(Properties props) {
		this.props = props;
	}
	
	public int getPort() {
		return Integer.parseInt((String)props.getOrDefault("appmain.port", "8080"));
	}
}
