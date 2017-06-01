package com.edocomar.demofeed.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.FeedArticles;


@Path("/articles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2017-06-01T12:04:23.300Z")


public class ArticlesApi  {
	private static final Logger logger = LoggerFactory.getLogger(ArticlesApi.class);	

	@GET
	@Path("/{user}")
	/**
    @ApiOperation(value = "", notes = "get list of new articles for feeds a user is subscribed to (required operation", response = FeedArticles.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = FeedArticles.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "user does not exist", response = Articles.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected Error", response = Articles.class, responseContainer = "List") })
	 */
	@Produces(MediaType.APPLICATION_JSON)
	public List<FeedArticles> articlesUserGet(@PathParam("user") String user) {
		
		List<FeedArticles> result = new ArrayList<>();
		FeedArticles feed1 = new FeedArticles();
		feed1.setFeed("feed1");
		Article a1 = new Article();
		a1.setTitle("title1");
		a1.setContent("content1");
		feed1.getArticles().add(a1);

		return result;
	}
}

