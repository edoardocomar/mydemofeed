package com.edocomar.demofeed;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.FeedArticles;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;

public class AppMainHttpTest {

	static File propFile;
	static final String BASEURI = "http://localhost:8081";
	
	@BeforeClass
	public static void setUp() throws Exception {
		Properties props = new Properties();
		props.put("appmain.port","8081");
		propFile = File.createTempFile("AppMainTest", "properties");
		
		try (FileWriter fw = new FileWriter(propFile)) {
			props.store(fw, "testMainStartShutDown");
		}
		AppMain.main(new String[]{ propFile.getAbsolutePath()});

		final long startTime = System.currentTimeMillis();
		while(!AppMain.isStarted()) {
			Thread.sleep(100L);
			if (System.currentTimeMillis()-startTime > 10000L){
				Assert.fail("Timeout");
			}
		}
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		try {
			AppMain.shutdown();
		} finally {
			propFile.delete();
		}
	}
	
	@Test(timeout=10000)
	public void testRootResource() throws Exception {
		GetRequest getRequest = Unirest.get(BASEURI);
		HttpResponse<String> response = getRequest.asString();
		assertEquals(200, response.getStatus());
		assertEquals(RootResource.BODY, response.getBody());
	}

	@Test(timeout=10000)
	public void testArticlesApi() throws Exception {
		List<FeedArticles> body = new ArrayList<>();
		FeedArticles feed1 = new FeedArticles();
		feed1.setFeed("feed1");
		Article a1 = new Article();
		a1.setTitle("title1");
		a1.setContent("content1");
		feed1.getArticles().add(a1);
		
		GetRequest getRequest = Unirest.get(BASEURI + "/articles/user1");
		HttpResponse<String> response = getRequest.asString();
		assertEquals(200, response.getStatus());
		String jsonString = response.getBody();
		ObjectMapper om = new ObjectMapper();
		FeedArticles[] readValue = om.readValue(jsonString, FeedArticles[].class);
		assertEquals(body, Arrays.asList(readValue));
//		get().then().statusCode(equalTo(200)).and().body(equalTo(body));
	}
}
