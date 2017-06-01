package com.edocomar.demofeed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal resource used for testing HTTP server functionality 
 * @author ecomar
 */
@Path("/")
public class RootResource {
	public static final String BODY = "OK!\n";
	private static final Logger logger = LoggerFactory.getLogger(RootResource.class);	
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getRoot() {
    	logger.info("GET /");
        return BODY;
    }
}