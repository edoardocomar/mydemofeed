package com.edocomar.demofeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.ErrorMessage;
import com.edocomar.demofeed.model.FeedArticles;
import com.edocomar.demofeed.util.RootResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;

/**
 * Integration test using the in-memory backend to test the Web API
 * <p>
 * Starts Jetty only once for speed.
 * Implemented a long script-like test to avoid test ordering issues with existing data  
 * @author ecomar
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class AppMainInMemoryHttpTest {

	static File propFile;
	static final String BASEURI = "http://localhost:8082";
	
	@BeforeClass
	public static void setUp() throws Exception {
		Properties props = new Properties();
		props.setProperty("appmain.port","8082");
		props.setProperty("predefined.feeds","feed1,feed2");
		propFile = File.createTempFile("AppMainTest", "properties");
		
		try (FileWriter fw = new FileWriter(propFile)) {
			props.store(fw, "AppMainHttpTest");
		}
		AppMain.main(new String[]{ propFile.getAbsolutePath(), "--in-memory"});

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
	public void testFeedsGetApi() throws Exception {
		GetRequest getRequest = Unirest.get(BASEURI + "/feeds");
		HttpResponse<String> response = getRequest.asString();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));
		
		Set<String> readValue = new ObjectMapper().readValue(response.getBody(), Set.class);
		HashSet expected = new HashSet(Arrays.asList("feed1","feed2"));
		assertEquals(expected, readValue);
	}
	
	@Test(timeout=10000)
	public void testSubscriptionsAndFeeds() throws Exception {
		// create 4 subscriptions, 2 for user1 and 2 for user2
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user1/feed1")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertEquals(201, response.getStatus());
		}
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user1/feed2")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertEquals(201, response.getStatus());
		}
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user1/feed2")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertEquals(200, response.getStatus());
		}
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user2/feed1")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertEquals(201, response.getStatus());
		}
		
		// check subscribed users
		{
			GetRequest getRequest = Unirest.get(BASEURI + "/subscriptions");
			HttpResponse<String> response = getRequest.asString();
			assertEquals(200, response.getStatus());
			assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));

			Set<String> readValue = new ObjectMapper().readValue(response.getBody(), Set.class);
			assertEquals(new HashSet(Arrays.asList("user1","user2")), readValue);
		}
		
		// check subscriptions for user1
		{
			GetRequest getRequest = Unirest.get(BASEURI + "/subscriptions/user1");
			HttpResponse<String> response = getRequest.asString();
			assertEquals(200, response.getStatus());
			assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));

			Set<String> readValue = new ObjectMapper().readValue(response.getBody(), Set.class);
			assertEquals(new HashSet(Arrays.asList("feed1","feed2")), readValue);
		}
		
		// remove feed1 from user1
		{
			HttpRequestWithBody deleteRequest = Unirest.delete(BASEURI + "/subscriptions/user1/feed1");
			HttpResponse<String> response = deleteRequest.asString();
			assertEquals(200, response.getStatus());
		}

		// check feed1 has been removed from user1
		{
			GetRequest getRequest = Unirest.get(BASEURI + "/subscriptions/user1");
			HttpResponse<String> response = getRequest.asString();
			assertEquals(200, response.getStatus());
			assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));

			Set<String> readValue = new ObjectMapper().readValue(response.getBody(), Set.class);
			assertEquals(new HashSet(Arrays.asList("feed2")), readValue);
		}

		// check can't subscribe user to non-existing feed
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user1/feedX")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertEquals(404, response.getStatus());
			assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));
			ErrorMessage readValue = new ObjectMapper().readValue(response.getBody(), ErrorMessage.class);
			assertTrue(readValue.getMsg(), readValue.getMsg().contains("feedX"));
		}
		
		// subscribe user3
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user3/feed1")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertEquals(201,response.getStatus());
		}
		
		// post two articles to feed1
		List<Article> articlesPosted = Arrays.asList(new Article("title1","content1"),new Article("title2","content2"));
		{
			ObjectMapper om = new ObjectMapper();
			String body = om.writeValueAsString(articlesPosted);
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/feeds" + "/feed1")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body(body);
			HttpResponse<String> response = postRequest.asString();
			assertEquals(200, response.getStatus());
		}

		// retrieve articles for user3
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
