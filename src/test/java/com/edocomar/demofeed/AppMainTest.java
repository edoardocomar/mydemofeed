package com.edocomar.demofeed;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

public class AppMainTest {

	File propFile;
	
	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();
		props.put("appmain.port","8081");
		propFile = File.createTempFile("AppMainTest", "properties");
		
		try (FileWriter fw = new FileWriter(propFile)) {
			props.store(fw, "testMainStartShutDown");
		}
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			AppMain.shutdown();
		} finally {
			propFile.delete();
		}
	}
	
	@Test(timeout=10000)
	public void testMainStartShutDown() throws Exception {
		AppMain.main(new String[]{ propFile.getAbsolutePath()});
		RestAssured.baseURI="http://localhost:8081";
		
		while(!AppMain.isStarted()) {
			Thread.sleep(100L);
		}
		
		get("/").then().statusCode(equalTo(200)).and().body(equalTo(RootResource.BODY));
	}

}
