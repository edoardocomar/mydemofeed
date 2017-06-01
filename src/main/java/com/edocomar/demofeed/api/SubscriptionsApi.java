package com.edocomar.demofeed.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
import com.edocomar.demofeed.model.ErrorMessage;


@Path("/subscriptions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SubscriptionsApi  {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsApi.class);
	
	private final AppContext appContext;
	
	public SubscriptionsApi(AppContext appContext) {
		this.appContext = appContext;
	}
	
	@GET
	/**
    @ApiOperation(value = "", notes = "returns list of known users (not req)", response = String.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "return list of feeds", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected Error", response = String.class, responseContainer = "List") })
	 */
	public Set<String> subscriptionsGet() {
		//TODO synch
		// subscriptions.keySet() it will be iterated over when adding to the list
		// coming from a concurrent map, the iterator is safe though weakly consistent
		return new HashSet<String>(appContext.subscriptions().keySet());
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
		if (!appContext.feeds().contains(feed)) {
			return Response.status(Status.NOT_FOUND).entity(new ErrorMessage("Feed " + feed + " not found")).build();
		}
		
		boolean created = false;
		Set<String> userSubs = appContext.subscriptions().get(user);
		if (userSubs==null) {
			created = true;
			userSubs = Collections.synchronizedSet(new HashSet<String>());
			userSubs.add(feed);
			appContext.subscriptions().put(user, userSubs);
		} else {
			created = userSubs.add(feed);
		}
		
		if (created) {
			return Response.status(Status.CREATED).build();
		} else {
			return Response.ok().build();
		}
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
		Set<String> userSubs = appContext.subscriptions().get(user);
		if (userSubs==null) {
			return Response.status(Status.NOT_FOUND).entity(new ErrorMessage("User " + user + " not found")).build();
		}
		boolean removed = userSubs.remove(feed);
		if (removed) {
			return Response.ok().build();
		} else {
			return Response.status(Status.NOT_FOUND).entity(new ErrorMessage("User " + user + " not subscribed to " + feed)).build();
		}
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
	public Collection<String> subscriptionsUserGet(@PathParam("user") String user) {
		Set<String> userSubs = appContext.subscriptions().get(user);
		if (userSubs==null) {
			Response response = Response.status(Status.NOT_FOUND).entity(new ErrorMessage("User " + user + " not found")).build();
			throw new NotFoundException(response);
		}
		// SYNCH : the set will be iterated over. 
		return userSubs;
	}
}

