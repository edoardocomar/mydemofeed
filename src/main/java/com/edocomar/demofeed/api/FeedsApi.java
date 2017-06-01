package com.edocomar.demofeed.api;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edocomar.demofeed.AppBackend;
import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.ErrorMessage;


@Path("/feeds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FeedsApi  {
	private static final Logger logger = LoggerFactory.getLogger(FeedsApi.class);
	private final AppBackend backend;	

	public FeedsApi(AppBackend backend) {
		this.backend = backend;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	/**
    @ApiOperation(value = "", notes = "returns list of defined feeds (was not a req)", response = String.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "return list of feeds", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected Error", response = String.class, responseContainer = "List") })
	 */
	public Set<String> feedsGet() {
    	logger.debug("GET /feeds ");
		return backend.config().availableFeeds();
	}
	
	@POST
	@Path("/{feed}")
	/**
    @ApiOperation(value = "", notes = "add articles to a feed (required operation", response = void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK article added", response = void.class),
        @ApiResponse(code = 400, message = "inavlid input data", response = void.class),
        @ApiResponse(code = 404, message = "feed does not exist", response = void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = void.class) })
	 */
	public Response feedsFeedPost(@PathParam("feed") String feed, List<Article> articles) throws Exception {
    	logger.debug("POST /feeds/" + feed + " articles.size="+articles.size());
		if(!backend.config().availableFeeds().contains(feed)) {
			return Response.status(Status.NOT_FOUND).entity(new ErrorMessage("Feed " + feed + " not found")).build();
		}
		
		backend.postArticles(feed,articles);
		return Response.ok().build();
	}

}

