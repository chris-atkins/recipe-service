package com.poorknight.persistence;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.poorknight.app.init.MongoSetup;
import com.poorknight.domain.User;
import com.poorknight.domain.identities.UserId;

public class UserRepository {

	private final MongoClient mongoClient;

	public UserRepository(final MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public User saveUser(final User userToSave) {
		final MongoCollection<Document> userCollection = getUserCollection();

		final Document document = toDocument(userToSave);
		userCollection.insertOne(document);
		return toUser(document);
	}

	public User findUserById(final UserId id) {
		final MongoCollection<Document> userCollection = getUserCollection();
		final Bson filter = Filters.eq("_id", new ObjectId(id.getValue()));
		final Document result = userCollection.find(filter).first();
		return toUser(result);
	}

	public User findUserByEmail(final String email) {
		final MongoCollection<Document> userCollection = getUserCollection();
		final Bson filter = Filters.text(email);
		final Document result = userCollection.find(filter).first();
		return toUser(result);
	}

	private Document toDocument(final User user) {
		return new Document().append("name", user.getName()).append("email", user.getEmail());
	}

	private User toUser(final Document document) {
		final UserId id = new UserId(document.getObjectId("_id").toHexString());
		final String name = document.getString("name");
		final String email = document.getString("email");

		return new User(id, name, email);
	}

	private MongoCollection<Document> getUserCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(MongoSetup.DB_NAME);
		return database.getCollection(MongoSetup.USER_COLLECTION);
	}
}
