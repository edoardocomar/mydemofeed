package com.edocomar.demofeed.api;


import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/subscriptions")

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2017-06-01T12:04:23.300Z")
public class SubscriptionsApi  {
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsApi.class);	
	@GET
	/**
    @ApiOperation(value = "", notes = "returns list of known users (not req)", response = String.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "return list of feeds", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected Error", response = String.class, responseContainer = "List") })
	 */
	public List<String> subscriptionsGet() {
		return Arrays.asList("user1","user2");
	}

	@DELETE
	@Path("/{user}/{feed}")
	/**
    @ApiOperation(value = "", notes = "Unsubscribe a user from a feed. (required operation", response = void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = void.class),
        @ApiResponse(code = 404, message = "user or feed does not exist", response = void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = void.class) })
	 */
	public Response subscriptionsUserFeedDelete(@PathParam("user") String user,@PathParam("feed") String feed) {
		return Response.ok().entity("magic!").build();
	}

	@POST
	@Path("/{user}/{feed}")
	/**
    @ApiOperation(value = "", notes = "Subscribe a user to a feed. Creates the user if needed and adds the feed to him/her if the feed is defined. (required operation", response = void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK (subscription may already exist)", response = void.class),
        @ApiResponse(code = 201, message = "OK subscription (and possibly user) created", response = void.class),
        @ApiResponse(code = 400, message = "Feed does not exist", response = void.class),
        @ApiResponse(code = 500, message = "Unexpected Error", response = void.class) })
	 */
	public Response subscriptionsUserFeedPost(@PathParam("user") String user,@PathParam("feed") String feed) {
		return Response.ok().entity("magic!").build();
	}

	@GET
	@Path("/{user}")
	/**
	@ApiOperation(value = "", notes = "return list of feeds the user is subscribed to. (required operation", response = String.class, responseContainer = "List", tags={  })
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "return list of feeds for user", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "user does not exist", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Unexpected Error", response = String.class, responseContainer = "List") })
	 */
	public List<String> subscriptionsUserGet(@PathParam("user") String user) {
		return Arrays.asList("feed1","feed2");
	}
}

