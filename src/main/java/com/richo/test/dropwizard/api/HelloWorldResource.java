package com.richo.test.dropwizard.api;

import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.richo.test.dropwizard.model.Saying;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource implements HelloWorldApi {
	private static final AtomicLong initCounter = new AtomicLong();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String template;
	private final String defaultName;
	private final AtomicLong counter;
	private final MongoClient mongoClient;

	public HelloWorldResource(final String template, final String defaultName, final MongoClient mongoClient) {
		this.template = template;
		this.defaultName = defaultName;
		this.mongoClient = mongoClient;
		this.counter = new AtomicLong();
		logger.warn("HelloWorldResource number {} created", initCounter.incrementAndGet());
	}

	@GET
	@Timed
	@Override
	public Saying sayHello(final Optional<String> name) {
		logger.info("{} is using sayHello Api", name.or("Unknown"));
		final String value = String.format(template, name.or(defaultName));
		return new Saying(counter.incrementAndGet(), value);
	}

	@GET
	@Timed
	@Path("/hi")
	@Override
	public String hi() {
		final MongoDatabase database = mongoClient.getDatabase("test");
		final MongoCollection<Document> collection = database.getCollection("balls");

		final Document myDoc = collection.find().first();
		System.out.println(myDoc.toJson());

		return myDoc.toJson();
	}
}
