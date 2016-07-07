package com.poorknight.recipebook;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.poorknight.application.init.MongoSetup;
import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class RecipeBookRepository {

	private final MongoClient mongoClient;

	public RecipeBookRepository(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	private MongoCollection<Document> getRecipeBookCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(MongoSetup.DB_NAME);
		return database.getCollection(RecipeBookCollectionInitializer.RECIPE_BOOK_COLLECTION);
	}

	public RecipeBook getRecipeBook(UserId userId) {
		final MongoCollection<Document> collection = getRecipeBookCollection();
		final Document recipeBookDocument = findRecipeBook(userId, collection);
		return toRecipeBook(recipeBookDocument);
	}

	public RecipeId addRecipeIdToRecipeBook(UserId userId, RecipeId recipeId) {
		final MongoCollection<Document> collection = getRecipeBookCollection();
		final Document existingDocument = findRecipeBook(userId, collection);

		if(existingDocument == null) {
			insertNewRecipeBook(userId, recipeId, collection);
		}
		else {
			updateExistingRecipeBookIfNeeded(collection, existingDocument, userId, recipeId);
		}
		return recipeId;
	}

	private Document findRecipeBook(UserId userId, MongoCollection<Document> collection) {
		final Bson userFilter = userIdFilter(userId);
		return collection.find(userFilter).first();
	}

	private Bson userIdFilter(UserId userId) {
		return Filters.eq("userId", new ObjectId(userId.getValue()));
	}

	private void insertNewRecipeBook(UserId userId, RecipeId recipeId, MongoCollection<Document> collection) {
		Document recipeBookDocument = buildRecipeBook(userId, new ArrayList<>(), recipeId);
		collection.insertOne(recipeBookDocument);
	}

	private void updateExistingRecipeBookIfNeeded(MongoCollection<Document> collection, Document existingDocument, UserId userId, RecipeId recipeId) {
		final ArrayList<ObjectId> recipeIds = existingDocument.get("recipeIds", ArrayList.class);
		if (recipeIds.contains(new ObjectId(recipeId.getValue()))) {
			return;
		}

		Document recipeBookDocument = buildRecipeBook(userId, recipeIds, recipeId);
		collection.findOneAndReplace(userIdFilter(userId), recipeBookDocument);
	}

	private Document buildRecipeBook(UserId userId, ArrayList<ObjectId> existingRecipeIds, RecipeId recipeIdToAdd) {
		existingRecipeIds.add(new ObjectId(recipeIdToAdd.getValue()));
		return new Document("userId", new ObjectId(userId.getValue())).append("recipeIds", existingRecipeIds);
	}

	private RecipeBook toRecipeBook(Document recipeBookDocument) {
		if (recipeBookDocument == null) {
			return null;
		}
		final String userIdString = recipeBookDocument.getObjectId("userId").toHexString();
		final ArrayList<ObjectId> recipeObjectIds = recipeBookDocument.get("recipeIds", ArrayList.class);

		List<RecipeId> recipeIds = new ArrayList<>(recipeObjectIds.size());
		for(ObjectId id : recipeObjectIds) {
			recipeIds.add(new RecipeId(id.toHexString()));
		}

		return new RecipeBook(new UserId(userIdString), recipeIds);
	}
}
