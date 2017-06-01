package com.edocomar.demofeed.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edocomar.demofeed.model.Article;


@Path("/feeds")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2017-06-01T12:04:23.300Z")
public class FeedsApi  {
	private static final Logger logger = LoggerFactory.getLogger(FeedsApi.class);	

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
		return Response.ok().build();
	}

	@GET
	/**
    @ApiOperation(value = "", notes = "returns list of defined feeds (not req)", response = String.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "return list of feeds", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected Error", response = String.class, responseContainer = "List") })
	 */
	public List<String> feedsGet() {
		List<String> feeds = new ArrayList<>();
		feeds.add("feed1");
		return feeds;
	}
}

