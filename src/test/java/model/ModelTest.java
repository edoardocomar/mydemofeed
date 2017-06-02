package model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.FeedArticles;

public class ModelTest {

	@Test
	public void testArticleIsValueObject() throws Exception {
		Article a1 = new Article();
		Article a2 = new Article();
		assertEquals(a1,a2);
		assertEquals(a1.hashCode(),a2.hashCode());
		assertEquals(a1.toString(),a2.toString());
		a1.setContent("content");
		a2.setContent("content");
		a1.setTitle("title");
		a2.setTitle("title");
		assertEquals(a1,a2);
		assertEquals(a1.hashCode(),a2.hashCode());
		assertEquals(a1.toString(),a2.toString());
		a2.setTitle("title2");
		assertNotEquals(a1,a2);
	}

	@Test
	public void testFeedArticleIsValueObject() throws Exception {
		FeedArticles fa1 = new FeedArticles();
		FeedArticles fa2 = new FeedArticles();
		assertEquals(fa1,fa2);
		assertEquals(fa1.hashCode(),fa2.hashCode());
		assertEquals(fa1.toString(),fa2.toString());
		fa1.setFeed("feed");
		fa2.setFeed("feed");
		Article a1 = new Article();
		Article a2 = new Article();
		a1.setContent("content");
		a2.setContent("content");
		a1.setTitle("title");
		a2.setTitle("title");
		fa1.getArticles().add(a1);
		fa1.getArticles().add(a2);
		assertNotEquals(fa1,fa2);
		fa2.getArticles().add(a1);
		fa2.getArticles().add(a2);
		assertEquals(fa1,fa2);
		assertEquals(fa1.hashCode(),fa2.hashCode());
		assertEquals(fa1.toString(),fa2.toString());
	}
}
