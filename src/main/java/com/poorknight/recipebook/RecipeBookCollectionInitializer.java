package com.poorknight.recipebook;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.poorknight.application.init.MongoCollectionInitializer;
import org.bson.Document;

public class RecipeBookCollectionInitializer implements MongoCollectionInitializer {

	public static final String RECIPE_BOOK_COLLECTION = "recipeBook";

	@Override
	public void initializeCollection(final MongoDatabase database) {
		final MongoCollection<Document> userCollection = database.getCollection(RECIPE_BOOK_COLLECTION);
		userCollection.createIndex(new Document("userId", 1), new IndexOptions().unique(true));
	}

}
