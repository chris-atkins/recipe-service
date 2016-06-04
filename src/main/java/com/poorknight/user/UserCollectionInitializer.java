package com.poorknight.user;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.poorknight.application.init.MongoCollectionInitializer;

public class UserCollectionInitializer implements MongoCollectionInitializer {

	public static final String USER_COLLECTION = "user";

	@Override
	public void initializeCollection(final MongoDatabase database) {
		final MongoCollection<Document> userCollection = database.getCollection(USER_COLLECTION);
		userCollection.createIndex(new Document("email", 1), new IndexOptions().unique(true));
	}

}
