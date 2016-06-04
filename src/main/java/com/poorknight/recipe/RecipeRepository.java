package com.poorknight.recipe;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.poorknight.application.init.MongoSetup;
import com.poorknight.recipe.search.SearchTag;

public class RecipeRepository {

	private final MongoClient mongoClient;

	public RecipeRepository(final MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public Recipe saveNewRecipe(final Recipe recipe) {
		throwExceptionIfARecipeIdExists(recipe);

		final MongoCollection<Document> collection = getRecipeCollection();
		final Document document = toDocument(recipe);
		collection.insertOne(document);
		return toRecipe(document);
	}

	private void throwExceptionIfARecipeIdExists(final Recipe recipe) {
		if (recipe.getId() != null) {
			throw new RuntimeException("Only new recipes can be saved in this way.  There should not be a RecipeId, but one was found: " + recipe.getId().getValue());
		}
	}

	public Recipe findRecipeById(final RecipeId id) {
		if (idIsNotValid(id)) {
			return null;
		}

		final MongoCollection<Document> collection = getRecipeCollection();
		final Bson idFilter = createFilterOnId(id);
		final Document document = collection.find(idFilter).first();
		return toRecipe(document);
	}

	private boolean idIsNotValid(final RecipeId id) {
		return !ObjectId.isValid(id.getValue());
	}

	public List<Recipe> findAllRecipes() {
		final MongoCollection<Document> collection = getRecipeCollection();
		final MongoCursor<Document> recipeIterator = collection.find().iterator();
		return toRecipeList(recipeIterator);
	}

	public List<Recipe> searchRecipes(final List<SearchTag> searchTags) {
		final MongoCollection<Document> collection = getRecipeCollection();
		final Bson recipeWithAnyTag = buildQueryForAnyTagFound(searchTags);
		final MongoCursor<Document> recipeIterator = collection.find(recipeWithAnyTag).iterator();
		return toRecipeList(recipeIterator);
	}

	private Bson buildQueryForAnyTagFound(final List<SearchTag> searchTags) {
		final StringBuilder sb = new StringBuilder();
		for (final SearchTag tag : searchTags) {
			sb.append(" ").append(tag.getValue());
		}

		final Bson anyOfTagsFilter = Filters.text(sb.toString().trim());
		return anyOfTagsFilter;
	}

	public void deleteRecipe(final RecipeId id) {
		final MongoCollection<Document> collection = getRecipeCollection();
		final Bson idFilter = createFilterOnId(id);
		collection.deleteOne(idFilter);
	}

	private MongoCollection<Document> getRecipeCollection() {
		final MongoDatabase database = this.mongoClient.getDatabase(MongoSetup.DB_NAME);
		return database.getCollection(RecipeCollectionInitializer.RECIPE_COLLECTION);
	}

	private Bson createFilterOnId(final RecipeId id) {
		return com.mongodb.client.model.Filters.eq("_id", new ObjectId(id.getValue()));
	}

	private Document toDocument(final Recipe recipe) {
		return new Document("name", recipe.getName()).append("content", recipe.getContent());
	}

	private Recipe toRecipe(final Document document) {
		if (document == null) {
			return null;
		}

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
