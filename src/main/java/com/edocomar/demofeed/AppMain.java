package com.edocomar.demofeed;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edocomar.demofeed.api.ArticlesApi;
import com.edocomar.demofeed.api.FeedsApi;
import com.edocomar.demofeed.api.SubscriptionsApi;

/**
 * Main class starting an embedded Jetty Server.
 * 
 * @author ecomar
 */
public class AppMain {
	private static final Logger logger = LoggerFactory.getLogger(AppMain.class);	
	private static AppMain instance;
	private Server jettyServer;

	/**
	 * @param args[0] property filename
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if(args.length != 1) {
			System.err.println("Missing required argument.\nUSAGE: java ... AppMain propertiesFilename");
			System.exit(1);
		}
		
		String propFile = args[0];
		
		AppConfig appConfig = new AppConfig(propFile);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					shutdown();
				} catch (Exception e) {
					logger.error("Shutdown error", e);
				}
			}
		});

		instance = new AppMain(appConfig);
		instance.start();
	}

	/*
	 * shutdown hook usable for testing
	 * @throws Exception
	 */
	static void shutdown() throws Exception {
		logger.info("Shutdown received");
		if(instance!=null) {
			instance.jettyServer.stop();
			instance.jettyServer.destroy();
		}
	}

	public AppMain (AppConfig appConfig) throws Exception { 

		RootResource rootResource = new RootResource();
		ArticlesApi articlesApi = new ArticlesApi();
		FeedsApi feedsApi = new FeedsApi();
		SubscriptionsApi subscriptionsApi = new SubscriptionsApi();

		ResourceConfig rc = new ResourceConfig();
		rc.register(rootResource);
		rc.register(articlesApi);
		rc.register(feedsApi);
		rc.register(subscriptionsApi);

		ServletContainer sc = new ServletContainer(rc);
		ServletHolder holder = new ServletHolder(sc);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(holder, "/*");

		jettyServer = new Server(appConfig.getPort());
		jettyServer.setHandler(context);
	}

    public void start() throws Exception {
		logger.info("Starting server");
		jettyServer.start();
	}
    
    static boolean isStarted() {
    	return instance!=null && instance.jettyServer !=null && instance.jettyServer.isStarted();
    }

}