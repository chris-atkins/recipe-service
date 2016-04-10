package com.poorknight.persistence;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.poorknight.domain.Recipe;
import com.poorknight.domain.identities.RecipeId;

public class RecipeRepository {

	private static final String DB_NAME = "recipe_db";
	private static final String RECIPE_COLLECTION = "recipe";
	private final MongoClient mongoClient;

	public RecipeRepository(final MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public Recipe saveNewRecipe(final Recipe recipe) {
		final MongoCollection<Document> collection = buildCollection();
		final Document document = toDocument(recipe);
		collection.insertOne(document);
		return toRecipe(document);
	}

	public Recipe findRecipeById(final RecipeId id) {
		final MongoCollection<Document> collection = buildCollection();
		final Bson filter = com.mongodb.client.model.Filters.eq("_id", new ObjectId(id.getValue()));
		return toRecipe(collection.find(filter).first());
	}

	private MongoCollection<Document> buildCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(DB_NAME);
		final MongoCollection<Document> collection = database.getCollection(RECIPE_COLLECTION);
		return collection;
	}

	private Document toDocument(final Recipe recipe) {
		final Document document = new Document("name", recipe.getName()).append("content", recipe.getContent());
		return document;
	}

	private Recipe toRecipe(final Document document) {
		final ObjectId id = document.getObjectId("_id");
		final String name = document.getString("name");
		final String content = document.getString("content");

		return new Recipe(new RecipeId(id.toHexString()), name, content);
	}
}
