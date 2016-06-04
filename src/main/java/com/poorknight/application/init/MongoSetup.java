package com.poorknight.application.init;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.poorknight.recipe.RecipeCollectionInitializer;
import com.poorknight.user.UserCollectionInitializer;

public class MongoSetup {

	public static final String DB_NAME = "recipe_db";

	public static void setupDatabaseCollections(final MongoClient client) {
		final MongoDatabase database = client.getDatabase(MongoSetup.DB_NAME);

		new RecipeCollectionInitializer().initializeCollection(database);
		new UserCollectionInitializer().initializeCollection(database);
	}
}
