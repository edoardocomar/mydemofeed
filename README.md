# mydemofeed app

## Prerequisites
* JVM (tested with JDK 1.8.0_131 on macOS)
* Gradle (tested with Gradle 3.4.1)
* Kafka installation (tested with 0.10.2.1 for Scala 2.12)

## Build, test
1. `gradle build`
1. `gradle test` runs two integration tests using ports 8081 and 8082 
one with an in-memory backend and one assuming Kafka running
with default settings on localhost:9092 (plaintext) 

## installation, execution
1. `gradle installDist`
1. `cd build/install/mydemofeed`
1. edit `config/demofeed.properties` as required,  
ensure a Kafka broker/cluster is available and topics named as the feeds 
in the properties file have been created (or that auto creation is enabled)

1. `bin/mydemofeed config/demofeed.properties` 
1. Interactive testing with cURL  
    * subscribe `user1` to `feed1` :  
    `curl -X POST http://localhost:8080/subscriptions/user1/feed1`
    * post an article to `feed1` :  
    `curl -X POST -H "Content-Type: application/json"  http://localhost:8080/feeds/feed1 --data '[{ "title":"title1xxx","content":"content1xxx"}]'`
    * consume articles for `user1` :  
    `curl   http://localhost:8080/articles/user1`
    * if you try consuming again, nothing will be returned
1. stop app with CTRL-C / kill \<pid> (shutdown hook installed)

## API
 
 The REST API adopted is described via this 
 [swagger.yaml](docs/swagger.yaml) document


## Design and development notes
### simplifying assumptions
* No authentication/authorization as discussed.
* Article content modelled as just a string.  
  One could still use base64 encoding for storing binary data.
* Fixed number of feeds only
* No API for deleting a 'user' (API only allows removing feeds from its subscription set)  
Could have implemented deletion on removal of last feed

### Language/libraries
* Implemented a JAX-RS Java application as I'm familiar with the language
* I chose a regular java application with a main() and an embedded Jetty server as I find it
convenient for development rather than relying on a JEE server
### Persistence
* For persistence of the articles I chose to use a Kafka broker.
It's natural to map the feed of the problem statement to a topic. 
* With a fixed number of feeds/topics, the topics are to be pre-created or auto-created.
### Usage of Kafka
* Adding articles to a feed : maps naturally to a Kafka publisher publishing to a topic.
* A subscriber receiving its articles from the subscribed feeds : maps naturally to a Kafka consumer consuming from a set of subscribed topics.
* Persisting the subscriber position : maps naturally to the consumer committing offsets.  
The consumer group maps to the 'user' of the problem statement.
* auto offset commit is ok, as all articles returned by a poll are returned in the HTTP response, 
__HOWEVER__ should a failure occur trying to return the records, they will not be consumed again.
* I am assuming a Kafka service dedicated to the feed app.   
So there's no need to worry about conflicting names for topics or consumer groups.
* As Kafka client are single-threaded, to be used in the REST application, they could :  
  * be created for the duration of the api call.  
  this is likely to be expensive and slow (connection, retrieval of meta-data)
  * they could be pooled  
    * simple pool for producers, as they're interchangeable 
    * pool keyed on the 'user'/consumer group for consumers,
      * with resubscribtion check at every call as the set of feeds may have changed  
      * with a strategy to limit the total number of clients though
### limitations
* The API POST /feeds/feed will accept multiple articles, is synchronous, 
and returns http 500 if any of the articles was not persisted.
A more sophisticated API could be asynchronous, returning a URI to check progress

### Persistence of the 'user' i.e. of the subscription set
* the data to be persisted is essentially a map, keys are string(user name), values set of strings (the set of feed names, i.e. topic names )
* One option is to use a compacted kafka topic. On every update of a user's feed set, a message is produced to the topic - 
 message key is the username and value the latest set of feed names. Then the app can consume from this topic via a Kafka Streams KTable, which
 can provide the current feedset for every user
* Another option is to simply persist the full serialized map, either in a topic or on the filesystem or in a database.
* I chose local filesystem persistence with custom json serialization as it was easy :-)

### Development iterations
1. Gradle project definition with Main class that starts Jetty with Jersey (JAX-RS) component
2. Design of the REST API with swagger and generation of interfaces.
3. Implementation of the REST API backed by an in-memory data model
and junit-testing of the HTTP calls.
This phase wasted an unexpected amount of time as I encountered unexpected errors in
serializing the collections of POJOs. 
(MessageBodyWriter not found for media type=application/json, type=class java.util.ArrayList)
It turned out to be a bad choice of dependencies - a mix of org.glassfish.jersey and com.fasterxm.jackson
libraries 
4. Storing POSTed articles with kafka producers
5. Consumption of articles with kafka consumers
6. Persistence of the subscriptions
7. Pooling of Kafka producers (NOT DONE)
8. Pooling of the consumers (NOT DONE)


