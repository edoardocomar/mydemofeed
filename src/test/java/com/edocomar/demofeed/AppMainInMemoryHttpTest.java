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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;

@SuppressWarnings({"unchecked","rawtypes"})
public class AppMainInMemoryHttpTest {

	static File propFile;
	static final String BASEURI = "http://localhost:8081";
	
	@BeforeClass
	public static void setUp() throws Exception {
		Properties props = new Properties();
		props.setProperty("appmain.port","8081");
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
	public void testSubscriptionsPost404() throws Exception {
		RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user1/feedX")
				.header("Content-Type", MediaType.APPLICATION_JSON)
				.body("");
		HttpResponse<String> response = postRequest.asString();
		assertEquals(404, response.getStatus());
		assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));
		ErrorMessage readValue = new ObjectMapper().readValue(response.getBody(), ErrorMessage.class);
		assertTrue(readValue.getMsg().contains("feedX"));
	}
	
	
	@Test(timeout=10000)
	public void testSubscriptionsPostDeleteGetApi() throws Exception {
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
		
		{
			GetRequest getRequest = Unirest.get(BASEURI + "/subscriptions/user1");
			HttpResponse<String> response = getRequest.asString();
			assertEquals(200, response.getStatus());
			assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));

			Set<String> readValue = new ObjectMapper().readValue(response.getBody(), Set.class);
			assertEquals(new HashSet(Arrays.asList("feed1","feed2")), readValue);
		}
		
		{
			HttpRequestWithBody deleteRequest = Unirest.delete(BASEURI + "/subscriptions/user1/feed1");
			HttpResponse<String> response = deleteRequest.asString();
			assertEquals(200, response.getStatus());
		}

		{
			GetRequest getRequest = Unirest.get(BASEURI + "/subscriptions/user1");
			HttpResponse<String> response = getRequest.asString();
			assertEquals(200, response.getStatus());
			assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get("Content-Type").get(0));

			Set<String> readValue = new ObjectMapper().readValue(response.getBody(), Set.class);
			assertEquals(new HashSet(Arrays.asList("feed2")), readValue);
		}
	}
	
	@Test(timeout=10000)
	public void testSubscriptionsPostFeedsPostArticlesGetApi() throws Exception {
		{
			RequestBodyEntity postRequest = Unirest.post(BASEURI + "/subscriptions" + "/user3/feed1")
					.header("Content-Type", MediaType.APPLICATION_JSON)
					.body("");
			HttpResponse<String> response = postRequest.asString();
			assertTrue(response.getStatus()==201);
		}
		
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
