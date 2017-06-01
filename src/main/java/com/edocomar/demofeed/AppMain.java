package com.edocomar.demofeed;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * @param args - args[0] port
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		String propFile = args.length > 0 ? args[0] : "demofeed.properties";
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
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		RootResource resource = new RootResource();
		ResourceConfig rc = new ResourceConfig();
		rc.register(resource);

		ServletContainer sc = new ServletContainer(rc);
		ServletHolder holder = new ServletHolder(sc);
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