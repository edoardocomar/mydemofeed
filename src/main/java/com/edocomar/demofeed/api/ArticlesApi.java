package com.edocomar.demofeed.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edocomar.demofeed.AppBackend;
import com.edocomar.demofeed.model.ErrorMessage;
import com.edocomar.demofeed.model.FeedArticles;


@Path("/articles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ArticlesApi  {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ArticlesApi.class);

	private AppBackend backend;	

	public ArticlesApi(AppBackend backend) {
		this.backend = backend;
	}
	
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
	public Collection<FeedArticles> articlesUserGet(@PathParam("user") String user) throws Exception {
		
		Set<String> userFeeds = backend.subscriptions().get(user);
		if (userFeeds == null) {
			Response response = Response.status(Status.NOT_FOUND).entity(new ErrorMessage("User " + user + " not found")).build();
			throw new NotFoundException(response);
		}
		// userFeeds is a ConcurrentSet so will tolerate asynch changes during iteration 
		return backend.articlesFor(user, userFeeds);
	}
		
}

