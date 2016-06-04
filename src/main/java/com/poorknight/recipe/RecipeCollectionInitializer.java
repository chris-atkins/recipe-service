package com.poorknight.recipe;

import org.bson.Document;

import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.poorknight.application.init.MongoCollectionInitializer;

public class RecipeCollectionInitializer implements MongoCollectionInitializer {

	public static final String RECIPE_COLLECTION = "recipe";

	@Override
	public void initializeCollection(final MongoDatabase database) {
		final MongoCollection<Document> collection = database.getCollection(RECIPE_COLLECTION);
		collection.createIndex(new Document(new ImmutableMap.Builder<String, Object>().put("name", "text").put("content", "text").build()));
	}

}
