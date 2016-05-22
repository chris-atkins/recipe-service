package com.poorknight.app.init;

import org.bson.Document;

import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoSetup {

	public static final String DB_NAME = "recipe_db";
	public static final String RECIPE_COLLECTION = "recipe";

	public static void setupDatabaseCollections(final MongoClient client) {
		final MongoDatabase database = client.getDatabase(MongoSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(MongoSetup.RECIPE_COLLECTION);
		collection.createIndex(new Document(new ImmutableMap.Builder<String, Object>().put("name", "text").put("content", "text").build()));
	}
}
