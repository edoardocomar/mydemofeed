package com.edocomar.demofeed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

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
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaBackend extends AbstractBackend {
	private static final Logger logger = LoggerFactory.getLogger(KafkaBackend.class);

	public KafkaBackend(AppConfig config) {
		super(config);
		//TODO validate config for fail-fast
	}

	@Override
	public Collection<FeedArticles> articlesFor(String user, Set<String> userFeeds) throws Exception {
		Properties clientProps = new Properties();
		//TODO pick all properties from config file
		clientProps.put("bootstrap.servers", config().getProperty("bootstrap.servers", "localhost:9092"));
		clientProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") ; 
		clientProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		clientProps.put("enable.auto.commit", "true");  
		clientProps.put("auto.offset.reset", "earliest");
		clientProps.put("fetch.max.bytes", config().getProperty("fetch.max.bytes","4194304")); //4MB
		clientProps.put("group.id", user); 

		long pollTimeout = Long.parseLong(config().getProperty("consumer.poll.timeout.ms","2000"));
		Map<String,FeedArticles> result = new HashMap<>();

		// closeable consumer will automatically be closed and offset committed
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(clientProps)) {

			consumer.subscribe(userFeeds, new ConsumerRebalanceListener() {
				@Override
				public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
					logger.info("onPartitionsRevoked " + partitions);
				}

				@Override
				public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
					logger.info("onPartitionsAssigned " + partitions);
				}
			});

			logger.info("consumer subscribed to " + userFeeds);
			ObjectMapper om = new ObjectMapper();
			// TODO ensure consumer is not returning 0 CRs because it's not ready 
			ConsumerRecords<String, String> crsPolled = consumer.poll(pollTimeout);
			logger.info("polled records count=" + crsPolled.count());
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
		Properties clientProps = new Properties();
		//TODO pick all properties from config file
		clientProps.put("bootstrap.servers", config().getProperty("bootstrap.servers", "localhost:9092"));
		clientProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer") ; 
		clientProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

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


}
