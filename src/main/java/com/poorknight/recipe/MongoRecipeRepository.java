package com.poorknight.recipe;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.poorknight.application.init.MongoSetup;
import com.poorknight.recipe.Recipe.RecipeId;
import com.poorknight.recipe.Recipe.UserId;
import com.poorknight.recipe.exception.NoRecipeExistsForIdException;
import com.poorknight.recipe.search.SearchTag;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MongoRecipeRepository implements RecipeRepositoryInterface {

	private static final FindOneAndUpdateOptions UPDATE_OPTIONS = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);

	private final MongoClient mongoClient;

	public MongoRecipeRepository(final MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	@Override
	public Recipe saveNewRecipe(final Recipe recipe) {
		throwExceptionIfARecipeIdExists(recipe);

		final MongoCollection<Document> collection = getRecipeCollection();
		final Document document = toDocumentForSave(recipe);
		collection.insertOne(document);
		return toRecipe(document);
	}

	@Override
	public Recipe updateRecipe(final Recipe recipeToUpdate) {
		throwExceptionIfRecipeIdIsInvalid(recipeToUpdate);

		final Document updatedRecipe = performUpdate(recipeToUpdate);

		throwExceptionIfNoUpdateOccurred(recipeToUpdate, updatedRecipe);
		return toRecipe(updatedRecipe);
	}

	private Document performUpdate(final Recipe recipeToUpdate) {
		final MongoCollection<Document> collection = getRecipeCollection();
		final Bson idFilter = createFilterOnId(recipeToUpdate.getId());
		final Document document = new Document("$set", toDocumentForUpdate(recipeToUpdate));
		return collection.findOneAndUpdate(idFilter, document, UPDATE_OPTIONS);
	}

	@Override
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
		if (id == null) {
			return true;
		}
		return !ObjectId.isValid(id.getValue());
	}

	@Override
	public List<Recipe> findAllRecipes() {
		final MongoCollection<Document> collection = getRecipeCollection();
		final MongoCursor<Document> recipeIterator = collection.find().iterator();
		return toRecipeList(recipeIterator);
	}

	@Override
	public List<Recipe> findRecipesWithIds(final List<RecipeId> recipeIdsToFind) {
		final MongoCollection<Document> collection = getRecipeCollection();
		final Bson recipesById = buildQueryForRecipeIds(recipeIdsToFind);
		final MongoCursor<Document> recipeIterator = collection.find(recipesById).iterator();
		return toRecipeList(recipeIterator);
	}

	private Bson buildQueryForRecipeIds(final List<RecipeId> recipeIdsToFind) {
		final List<ObjectId> objectIds = new ArrayList<>(recipeIdsToFind.size());
		for (final RecipeId id : recipeIdsToFind) {
			if (isAValidObjectId(id)) {
				objectIds.add(new ObjectId(id.getValue()));
			}
		}
		return Filters.in("_id", objectIds);
	}

	private boolean isAValidObjectId(final RecipeId id) {
		return id != null && id.getValue() != null && ObjectId.isValid(id.getValue());
	}

	@Override
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

		return Filters.text(sb.toString().trim());
	}

	@Override
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
		return Filters.eq("_id", new ObjectId(id.getValue()));
	}

	private Document toDocumentForSave(final Recipe recipe) {
		return new Document("name", recipe.getName()) //
				.append("content", recipe.getContent()) //
				.append("owningUserId", recipe.getOwningUserId().getValue())
				.append("image", toDocumentForSave(recipe.getImage()));
	}

	private Document toDocumentForSave(final RecipeImage image) {
		if (image == null) {
			return null;
		}
		return new Document("imageId", image.getImageId()) //
				.append("imageUrl", image.getImageUrl());
	}

	private Document toDocumentForUpdate(final Recipe recipe) {
		return new Document("name", recipe.getName()) //
				.append("content", recipe.getContent())
				.append("image", toDocumentForSave(recipe.getImage()));
	}

	private Recipe toRecipe(final Document document) {
		if (document == null) {
			return null;
		}

		final ObjectId id = document.getObjectId("_id");
		final String name = document.getString("name");
		final String content = document.getString("content");
		final String owningUserId = document.getString("owningUserId");
		final RecipeImage image = findImageFromDocument((Document)document.get("image"));

		return new Recipe(new RecipeId(id.toHexString()), name, content, new UserId(owningUserId), image);
	}

	private RecipeImage findImageFromDocument(final Document document) {
		if (document == null) {
			return null;
		}
		return new RecipeImage(document.getString("imageId"), document.getString("imageUrl"));
	}

	private List<Recipe> toRecipeList(final MongoCursor<Document> recipeIterator) {
		final List<Recipe> recipeList = new LinkedList<>();
		while (recipeIterator.hasNext()) {
			final Document document = recipeIterator.next();
			recipeList.add(toRecipe(document));
		}
		return recipeList;
	}

	private void throwExceptionIfNoUpdateOccurred(final Recipe recipeToUpdate, final Document updatedRecipe) {
		if(updatedRecipe == null) {
			throwInvalidUpdateRecipeIdException(recipeToUpdate.getId());
		}
	}

	private void throwExceptionIfRecipeIdIsInvalid(final Recipe recipeToUpdate) {
		if (idIsNotValid(recipeToUpdate.getId())) {
			throwInvalidUpdateRecipeIdException(recipeToUpdate.getId());
		}
	}

	private void throwInvalidUpdateRecipeIdException(final RecipeId id) {
		throw new NoRecipeExistsForIdException(id);
	}

	private void throwExceptionIfARecipeIdExists(final Recipe recipe) {
		if (recipe.getId() != null) {
			throw new RuntimeException("Only new recipes can be saved in this way.  There should not be a RecipeId, but one was found: " + recipe.getId().getValue());
		}
	}
}