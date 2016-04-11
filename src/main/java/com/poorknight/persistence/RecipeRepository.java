package com.poorknight.persistence;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
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
		final MongoCollection<Document> collection = getRecipeCollection();
		final Document document = toDocument(recipe);
		collection.insertOne(document);
		return toRecipe(document);
	}

	public Recipe findRecipeById(final RecipeId id) {
		final MongoCollection<Document> collection = getRecipeCollection();
		final Bson idFilter = createFilterOnId(id);
		final Document document = collection.find(idFilter).first();
		return toRecipe(document);
	}

	public List<Recipe> findAllRecipes() {
		final MongoCollection<Document> collection = getRecipeCollection();
		final MongoCursor<Document> recipeIterator = collection.find().iterator();
		return toRecipeList(recipeIterator);
	}

	public void deleteRecipe(final RecipeId id) {
		final MongoCollection<Document> collection = getRecipeCollection();
		final Bson idFilter = createFilterOnId(id);
		collection.deleteOne(idFilter);
	}

	private MongoCollection<Document> getRecipeCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(DB_NAME);
		return database.getCollection(RECIPE_COLLECTION);
	}

	private Bson createFilterOnId(final RecipeId id) {
		return com.mongodb.client.model.Filters.eq("_id", new ObjectId(id.getValue()));
	}

	private Document toDocument(final Recipe recipe) {
		return new Document("name", recipe.getName()).append("content", recipe.getContent());
	}

	private Recipe toRecipe(final Document document) {
		final ObjectId id = document.getObjectId("_id");
		final String name = document.getString("name");
		final String content = document.getString("content");

		return new Recipe(new RecipeId(id.toHexString()), name, content);
	}

	private List<Recipe> toRecipeList(final MongoCursor<Document> recipeIterator) {
		final List<Recipe> recipeList = new LinkedList<>();
		while (recipeIterator.hasNext()) {
			final Document document = recipeIterator.next();
			recipeList.add(toRecipe(document));
		}
		return recipeList;
	}
}
