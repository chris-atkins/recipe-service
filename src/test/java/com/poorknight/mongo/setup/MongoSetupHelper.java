package com.poorknight.mongo.setup;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.poorknight.application.init.MongoSetup;
import com.poorknight.image.ImageCollectionInitializer;
import com.poorknight.recipe.RecipeCollectionInitializer;
import com.poorknight.recipebook.RecipeBookCollectionInitializer;
import com.poorknight.user.UserCollectionInitializer;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;

public class MongoSetupHelper {

	private static final String MONGO_VERSION = "3.2.4";

	private static MongodExecutable mongodExecutable = null;
	private static MongoClient mongo;
	private static MongodProcess mongod;

	public static MongoClient startMongoInstance() throws Exception {
		final MongodStarter starter = MongodStarter.getDefaultInstance();
		final int port = Network.getFreeServerPort();
		final IMongodConfig mongodConfig = new MongodConfigBuilder() //
				.version(Versions.withFeatures(new GenericVersion(MONGO_VERSION), Feature.SYNC_DELAY)) //
				.net(new Net(port, Network.localhostIsIPv6())) //
				.build();

		mongodExecutable = starter.prepare(mongodConfig);
		mongod = mongodExecutable.start();
		mongo = new MongoClient("localhost", port);
		MongoSetup.setupDatabaseCollections(mongo);
		return mongo;
	}

	public static void cleanupMongo() {
		try {
			mongo.close();
		} finally {
			mongod.stop();
			mongodExecutable.stop();
		}
	}

	public static void deleteAllRecipes() {
		final MongoDatabase database = mongo.getDatabase(MongoSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(RecipeCollectionInitializer.RECIPE_COLLECTION);
		collection.deleteMany(new Document());
	}

	public static void deleteAllUsers() {
		final MongoDatabase database = mongo.getDatabase(MongoSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(UserCollectionInitializer.USER_COLLECTION);
		collection.deleteMany(new Document());
	}

	public static void deleteAllRecipeBooks() {
		final MongoDatabase database = mongo.getDatabase(MongoSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(RecipeBookCollectionInitializer.RECIPE_BOOK_COLLECTION);
		collection.deleteMany(new Document());
	}

	public static void deleteAllImages() {
		final MongoDatabase database = mongo.getDatabase(MongoSetup.DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(ImageCollectionInitializer.IMAGE_COLLECTION);
		collection.deleteMany(new Document());
	}
}
