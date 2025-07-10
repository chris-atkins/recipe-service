package com.poorknight.mongo.setup;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.poorknight.application.init.DatabaseSetup;
import com.poorknight.image.ImageCollectionInitializer;
import com.poorknight.recipe.RecipeCollectionInitializer;
import com.poorknight.recipebook.RecipeBookCollectionInitializer;
import com.poorknight.user.UserCollectionInitializer;
import org.bson.Document;
import org.testcontainers.containers.MongoDBContainer;

public class MongoSetupHelper {

	private static MongoClient mongo;

	static public MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

	public static MongoClient startMongoInstance() throws Exception {
		mongoDBContainer.start();
		int port = mongoDBContainer.getFirstMappedPort();

		mongo = new MongoClient("localhost", port);
		DatabaseSetup.setupDatabaseCollections(mongo);
		return mongo;
	}

	public static void cleanupMongo() {
		mongoDBContainer.stop();
	}

	public static void deleteAllRecipes() {
		final MongoDatabase database = mongo.getDatabase(DatabaseSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(RecipeCollectionInitializer.RECIPE_COLLECTION);
		collection.deleteMany(new Document());
	}

	public static void deleteAllUsers() {
		final MongoDatabase database = mongo.getDatabase(DatabaseSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(UserCollectionInitializer.USER_COLLECTION);
		collection.deleteMany(new Document());
	}

	public static void deleteAllRecipeBooks() {
		final MongoDatabase database = mongo.getDatabase(DatabaseSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(RecipeBookCollectionInitializer.RECIPE_BOOK_COLLECTION);
		collection.deleteMany(new Document());
	}

	public static void deleteAllImages() {
		final MongoDatabase database = mongo.getDatabase(DatabaseSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(ImageCollectionInitializer.IMAGE_COLLECTION);
		collection.deleteMany(new Document());
	}
}
