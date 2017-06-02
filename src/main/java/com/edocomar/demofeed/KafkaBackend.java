package com.edocomar.demofeed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edocomar.demofeed.model.Article;
import com.edocomar.demofeed.model.FeedArticles;
import com.edocomar.demofeed.util.SubscriptionsSerde;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaBackend extends AbstractBackend {
	private static final Logger logger = LoggerFactory.getLogger(KafkaBackend.class);
	
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	public KafkaBackend(AppConfig config) throws Exception {
		super(config);
		//TODO validate config for fail-fast
		loadPersistedSubscriptions();
	}


	@Override
	public Collection<FeedArticles> articlesFor(String user, Set<String> userFeeds) throws Exception {
		Properties clientProps = newConsumerProps(user); 
		long pollTimeout = Long.parseLong(config().getProperty("consumer.poll.timeout.ms","2000"));
		Map<String,FeedArticles> result = new HashMap<>();

		// closeable consumer will automatically be closed and offset committed
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(clientProps)) {

			consumer.subscribe(userFeeds, new ConsumerRebalanceListener() {
				@Override
				public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
					logger.debug("consumer onPartitionsRevoked " + partitions);
				}

				@Override
				public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
					logger.debug("consumer onPartitionsAssigned " + partitions);
				}
			});

			logger.debug("consumer subscribed to " + userFeeds);
			ObjectMapper om = new ObjectMapper();
			// to ensure consumer is not returning 0 CRs because it's not ready. 
			// I am using a slightly high the pollTimeout parameter
			// this will make requests slow when no data is available
			ConsumerRecords<String, String> crsPolled = consumer.poll(pollTimeout);
			logger.info("consumer polled records count=" + crsPolled.count());
			
			for (ConsumerRecord<String, String> cr : crsPolled) {
				String feed = cr.topic();
				Article article = om.readValue(cr.value(), Article.class);
				if(!result.containsKey(feed)) {
					result.put(feed, new FeedArticles(feed));
				}
				result.get(feed).getArticles().add(article);
			}
		} 

		return result.values();
	}


	@Override
	public void postArticles(String feed, List<Article> newArticles) throws Exception {
		Properties clientProps = newProducerProps();

		List<Future<RecordMetadata>> futures = new ArrayList<>();
		
		// closeable producer will automatically be closed at the end
		try (Producer<String, String> producer = new KafkaProducer<>(clientProps)) {
			ObjectMapper om = new ObjectMapper();
			for (Article article : newArticles) {
				Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(feed, om.writeValueAsString(article)));
				futures.add(future);
			}
		}
		// simple synchronous implementation
		for (Future<RecordMetadata> future : futures) {
			future.get(); //exception will be mapped as HTTP 500
		}
	}


	private Properties newProducerProps() {
		Properties clientProps = new Properties();
		//TODO read any producer properties from config file
		clientProps.put("bootstrap.servers", config().getProperty("bootstrap.servers", "localhost:9092"));
		// TODO ArticleSerializer as improvement
		clientProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer") ; 
		clientProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		return clientProps;
	}

	private Properties newConsumerProps(String user) {
		Properties clientProps = new Properties();
		//TODO read any consumer properties from config file
		clientProps.put("bootstrap.servers", config().getProperty("bootstrap.servers", "localhost:9092"));
		clientProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		// TODO ArticleDeserializer as improvement
		clientProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		clientProps.put("enable.auto.commit", "true");  
		clientProps.put("auto.offset.reset", "earliest");
		clientProps.put("fetch.max.bytes", config().getProperty("fetch.max.bytes","4194304")); //4MB
		clientProps.put("group.id", user);
		return clientProps;
	}

	/**
	 * asynchronous implementations that queues request to save
	 * errors will only be logged 
	 */
	@Override
	public void persistSubscriptions() throws Exception {
		singleThreadExecutor.submit( new Runnable() {
			@Override
			public void run() {
				try(FileOutputStream fos = new FileOutputStream(persistentFile())) { 
					new SubscriptionsSerde().write(subscriptions(), fos);
					logger.debug("Stored persisted subscriptions");
				} catch (Exception e) {
					logger.error("Failed to store subscribtions", e);
				}
			}
		});
	}

	@Override
	public void shutdown() throws InterruptedException {
		singleThreadExecutor.shutdown();
		singleThreadExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
	}
	
	private void loadPersistedSubscriptions() throws Exception {
		File persistentFile = persistentFile();
		if (!persistentFile.exists() || persistentFile.length()==0) {
			logger.warn("Persistent file not found or empty: " + persistentFile());
			logger.warn("Starting application with empty subscriptions");
			return;
		}
		
		try (FileInputStream fis = new FileInputStream(persistentFile)) {
			new SubscriptionsSerde().readInto(subscriptions(), fis);
		} catch (Exception e) {
			logger.error("FATAL: Failed to deserialize subscribtions from " + persistentFile() +
					"\n"+e.getMessage()+
					"\nYou should delete the file and restart from empty.");
			throw e; 
		}
		logger.info("Loaded persisted subscriptions from " + persistentFile + "\n" + subscriptions() + "\n");
	}

	private File persistentFile() {
		String filename = config().getProperty("subscriptions.filename", System.getProperty("user.home") + "/.mydemofeed-subscriptions.json");
		return new File(filename);
	}
}
