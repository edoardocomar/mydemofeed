package com.edocomar.demofeed.api;

import java.util.HashSet;
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

import com.edocomar.demofeed.AppContext;
import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.ErrorMessage;


@Path("/feeds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FeedsApi  {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(FeedsApi.class);
	private final AppContext appContext;	

	public FeedsApi(AppContext appContext) {
		this.appContext = appContext;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	/**
    @ApiOperation(value = "", notes = "returns list of defined feeds (not req)", response = String.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "return list of feeds", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected Error", response = String.class, responseContainer = "List") })
	 */
	public Set<String> feedsGet() {
		return new HashSet<String>(appContext.feeds());
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
	public Response feedsFeedPost(@PathParam("feed") String feed, List<Article> articles) {
		if(!appContext.feeds().contains(feed)) {
			return Response.status(Status.NOT_FOUND).entity(new ErrorMessage("Feed " + feed + " not found")).build();
		}
		appContext.articles().get(feed).addAll(articles);
		return Response.ok().build();
	}

}

