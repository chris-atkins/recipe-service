package com.poorknight.user;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.poorknight.application.init.DatabaseSetup;
import com.poorknight.user.User.UserId;
import com.poorknight.user.save.NonUniqueEmailException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class MongoUserRepository implements UserRepository {

	private final MongoClient mongoClient;
	private final UserTranslator userTranslator;

	public MongoUserRepository(final MongoClient mongoClient, final UserTranslator userTranslator) {
		this.mongoClient = mongoClient;
		this.userTranslator = userTranslator;
	}

	@Override
	public User saveNewUser(final User userToSave) {
		try {
			return saveNewUserWithException(userToSave);

		} catch (final MongoWriteException e) {
			if (isDuplicateEmailError(e)) {
				throw new NonUniqueEmailException(userToSave.getEmail());
			}
			throw e;
		}
	}

	private User saveNewUserWithException(final User userToSave) {
		final MongoCollection<Document> userCollection = getUserCollection();

		final Document document = toDocument(userToSave);
		userCollection.insertOne(document);
		return toUser(document);
	}

	private boolean isDuplicateEmailError(final MongoWriteException e) {
		return e.getMessage().contains("duplicate key error") && e.getMessage().contains("email");
	}

	@Override
	public User findUserById(final UserId id) {
		final MongoCollection<Document> userCollection = getUserCollection();
		final Bson filter = Filters.eq("_id", new ObjectId(id.getValue()));
		final Document result = userCollection.find(filter).first();
		return toUser(result);
	}

	@Override
	public User findUserByEmail(final String email) {
		final MongoCollection<Document> userCollection = getUserCollection();
		final Bson filter = Filters.eq("email", email);
		final Document result = userCollection.find(filter).first();
		return toUser(result);
	}

	private Document toDocument(final User user) {
		return new Document().append("name", user.getName()).append("email", user.getEmail());
	}

	private User toUser(final Document document) {
		if (document == null) {
			return null;
		}

		final String userIdAsHexString = document.getObjectId("_id").toHexString();
		final UserId id = userTranslator.userIdFor(userIdAsHexString);
		final String name = document.getString("name");
		final String email = document.getString("email");

		return new User(id, name, email);
	}

	private MongoCollection<Document> getUserCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(DatabaseSetup.DB_NAME);
		return database.getCollection(UserCollectionInitializer.USER_COLLECTION);
	}
}