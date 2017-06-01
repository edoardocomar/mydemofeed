package com.edocomar.demofeed.api;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

import com.edocomar.demofeed.AppBackend;
import com.edocomar.demofeed.model.ErrorMessage;


@Path("/subscriptions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SubscriptionsApi  {
	
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsApi.class);
	
	private final AppBackend backend;
	
	public SubscriptionsApi(AppBackend backend) {
		this.backend = backend;
	}
	
	@GET
	/**
    @ApiOperation(value = "", notes = "returns list of known users (was not a req)", response = String.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "return list of feeds", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unexpected Error", response = String.class, responseContainer = "List") })
	 */
	public Set<String> usersGet() {
    	logger.debug("GET /subscriptions");
		Set<String> users = backend.subscriptions().keySet();
		// the set will be iterated over when serialized
		// and it is subject to be modified by other threads 
		// As subscriptions is a concurrent map, the iterator is safe though weakly consistent.
		return users;
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
	public Response subscriptionsUserFeedPost(@PathParam("user") String user,@PathParam("feed") String feed) throws Exception {
    	logger.debug("POST /subscriptions/"+user+"/"+feed);
		if (!backend.config().availableFeeds().contains(feed)) {
			return Response.status(Status.NOT_FOUND).entity(new ErrorMessage("Feed " + feed + " not found")).build();
		}
		
		ConcurrentMap<String, Set<String>> subscriptions = backend.subscriptions();
		// check-and-act in synchronized block to avoid a race :
		// if two threads come in with the same user,
		// without synch both may set a new empty set as value
		// Note that this is flagged by Findbugs as JLM_JSR166_UTILCONCURRENT_MONITORENTER
		synchronized(subscriptions) {
			if (!subscriptions.containsKey(user)) {
				//this is a trick to create a ConcurrentSet which is class missing from java.util package  
				subscriptions.put(user, ConcurrentHashMap.newKeySet());
			} 
		}

		// this is a ConcurrentSet, can be modified without synch 
		Set<String> userFeeds = subscriptions.get(user);
		if (userFeeds.add(feed)) {
			backend.persistSubscriptions();
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
	public Response subscriptionsUserFeedDelete(@PathParam("user") String user,@PathParam("feed") String feed) throws Exception {
    	logger.debug("DELETE /subscriptions/"+user+"/"+feed);
		// unsynchronized check-and-act is ok here
		// it's ok for the set to become non-null later
		Set<String> userFeeds = backend.subscriptions().get(user);
		if (userFeeds==null) {
			return Response.status(Status.NOT_FOUND).entity(new ErrorMessage("User " + user + " not found")).build();
		}
		
		// this is a ConcurrentSet, can be modified without synch 
		if (userFeeds.remove(feed)) {
			backend.persistSubscriptions();
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
    	logger.debug("GET /subscriptions/"+user);
		Set<String> userFeeds = backend.subscriptions().get(user);
		if (userFeeds==null) {
			Response response = Response.status(Status.NOT_FOUND).entity(new ErrorMessage("User " + user + " not found")).build();
			throw new NotFoundException(response);
		}

		// this is a ConcurrentSet so will tolerate asynch changes during iteration 
		return userFeeds;
	}
}

