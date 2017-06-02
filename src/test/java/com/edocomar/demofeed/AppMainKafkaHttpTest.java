package com.edocomar.demofeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.FeedArticles;
import com.edocomar.demofeed.util.RootResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.body.RequestBodyEntity;

/**
 * Integration testing that expects a running Kafka 
 * to be available on localhost:9092
 * with topic auto-creation enabled or pre-created topics
 * 
 * @author ecomar
 */
public class AppMainKafkaHttpTest {

	static File propFile;
	static final String BASEURI = "http://localhost:8081";
	
	@BeforeClass
	public static void setUp() throws Exception {
		Properties props = new Properties();
		props.put("appmain.port","8081");
		props.put("predefined.feeds","feed1,feed2");
		propFile = File.createTempFile("AppMainTest", "properties");
		
		try (FileWriter fw = new FileWriter(propFile)) {
			props.store(fw, "AppMainHttpTest2");
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
	public void testSubscribeProduceConsume() throws Exception {
		{
		GetRequest getRequest = Unirest.get(BASEURI);
		HttpResponse<String> response = getRequest.asString();
		assertEquals(200, response.getStatus());
		assertEquals(RootResource.BODY, response.getBody());
		}

		//create a subscription
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user3/feed1")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertTrue(""+response.getStatus(), response.getStatus()==201 || response.getStatus()==200);
		}
		
		//post articles with unique content
		long now = System.currentTimeMillis();
		List<Article> articlesPosted = Arrays.asList(new Article("title1","content1-"+now),
				new Article("title2","content2-"+now));
		{
			ObjectMapper om = new ObjectMapper();
			String body = om.writeValueAsString(articlesPosted);
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/feeds" + "/feed1")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body(body);
			HttpResponse<String> response = postRequest.asString();
			assertEquals(200, response.getStatus());
		}

		//get Articles
		{
			GetRequest getRequest = Unirest.get(BASEURI + "/articles/user3");
			HttpResponse<String> response = getRequest.asString();
			assertEquals(200, response.getStatus());
			assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));

			ObjectMapper om = new ObjectMapper();
			List<FeedArticles> readValue = om.readValue(response.getBody(), 
					om.getTypeFactory().constructCollectionType(List.class, FeedArticles.class));
			assertEquals(1, readValue.size());
			assertEquals("feed1", readValue.get(0).getFeed());
			assertEquals(articlesPosted, readValue.get(0).getArticles());
		}
	}
}
