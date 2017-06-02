package com.edocomar.demofeed.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class Utils {

	public static Properties loadProps(String propFilename) throws Exception {
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

	public static String propsToString(Properties props) {
		StringWriter sw = new StringWriter();
		props.list(new PrintWriter(sw));
		return sw.toString();
	}

}
