package com.poorknight.application.init;

import com.mongodb.client.MongoDatabase;

public interface MongoCollectionInitializer {

	void initializeCollection(MongoDatabase database);
}
