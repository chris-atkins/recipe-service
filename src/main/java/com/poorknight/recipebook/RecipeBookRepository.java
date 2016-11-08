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

	public RecipeBookRepository(final MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	private MongoCollection<Document> getRecipeBookCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(MongoSetup.DB_NAME);
		return database.getCollection(RecipeBookCollectionInitializer.RECIPE_BOOK_COLLECTION);
	}

	public RecipeBook getRecipeBook(final UserId userId) {
		final MongoCollection<Document> collection = getRecipeBookCollection();
		final Document recipeBookDocument = findRecipeBook(userId, collection);
		return toRecipeBook(recipeBookDocument);
	}

	public RecipeId addRecipeIdToRecipeBook(final UserId userId, final RecipeId recipeId) {
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

	private Document findRecipeBook(final UserId userId, final MongoCollection<Document> collection) {
		final Bson userFilter = userIdFilter(userId);
		return collection.find(userFilter).first();
	}

	private Bson userIdFilter(final UserId userId) {
		return Filters.eq("userId", buildObjectId(userId.getValue()));
	}

	private ObjectId buildObjectId(final String idString) {
		if(!ObjectId.isValid(idString)) {
			throw new InvalidIdException(idString);
		}
		return new ObjectId(idString);
	}

	private void insertNewRecipeBook(final UserId userId, final RecipeId recipeId, final MongoCollection<Document> collection) {
		final Document recipeBookDocument = buildRecipeBook(userId, new ArrayList<>(), recipeId);
		collection.insertOne(recipeBookDocument);
	}

	@SuppressWarnings("unchecked")
	private void updateExistingRecipeBookIfNeeded(final MongoCollection<Document> collection, final Document existingDocument, final UserId userId, final RecipeId recipeId) {
		final ArrayList<ObjectId> recipeIds = existingDocument.get("recipeIds", ArrayList.class);
		if (recipeIds.contains(buildObjectId(recipeId.getValue()))) {
			return;
		}

		final Document recipeBookDocument = buildRecipeBook(userId, recipeIds, recipeId);
		collection.findOneAndReplace(userIdFilter(userId), recipeBookDocument);
	}

	private Document buildRecipeBook(final UserId userId, final ArrayList<ObjectId> existingRecipeIds, final RecipeId recipeIdToAdd) {
		existingRecipeIds.add(buildObjectId(recipeIdToAdd.getValue()));
		return new Document("userId", buildObjectId(userId.getValue())).append("recipeIds", existingRecipeIds);
	}

	@SuppressWarnings("unchecked")
	private RecipeBook toRecipeBook(final Document recipeBookDocument) {
		if (recipeBookDocument == null) {
			return null;
		}
		final String userIdString = recipeBookDocument.getObjectId("userId").toHexString();
		final ArrayList<ObjectId> recipeObjectIds = recipeBookDocument.get("recipeIds", ArrayList.class);

		final List<RecipeId> recipeIds = new ArrayList<>(recipeObjectIds.size());
		for(final ObjectId id : recipeObjectIds) {
			recipeIds.add(new RecipeId(id.toHexString()));
		}

		return new RecipeBook(new UserId(userIdString), recipeIds);
	}

	@SuppressWarnings("unchecked")
	public void deleteRecipeFromRecipeBook(final UserId userId, final RecipeId recipeId) {
		final MongoCollection<Document> collection = getRecipeBookCollection();

		final Document existingDocument = findRecipeBook(userId, collection);
		if (existingDocument == null) {
			throw new RecipeBookNotFoundException(userId);
		}
		final ArrayList<ObjectId> recipeIds = existingDocument.get("recipeIds", ArrayList.class);

		final ObjectId objectIdForRecipe = buildObjectId(recipeId.getValue());
		if (!recipeIds.contains(objectIdForRecipe)) {
			throw new RecipeNotInBookException(recipeId);
		}
		recipeIds.remove(objectIdForRecipe);

		final Document recipeBookDocument = buildRecipeBook(userId, recipeIds);
		collection.findOneAndReplace(userIdFilter(userId), recipeBookDocument);
	}

	private Document buildRecipeBook(final UserId userId, final ArrayList<ObjectId> recipeIds) {
		return new Document("userId", buildObjectId(userId.getValue())).append("recipeIds", recipeIds);
	}
}
