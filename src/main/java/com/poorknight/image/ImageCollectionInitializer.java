package com.poorknight.image;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.poorknight.application.init.MongoCollectionInitializer;
import org.bson.Document;

public class ImageCollectionInitializer implements MongoCollectionInitializer {

	public static final String IMAGE_COLLECTION = "image";

	@Override
	public void initializeCollection(final MongoDatabase database) {
		final MongoCollection<Document> userCollection = database.getCollection(IMAGE_COLLECTION);
		userCollection.createIndex(new Document("imageId", 1), new IndexOptions().unique(true));
	}
}
