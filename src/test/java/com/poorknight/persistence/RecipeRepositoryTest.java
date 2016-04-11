package com.poorknight.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.mongodb.MongoClient;
import com.poorknight.domain.Recipe;
import com.poorknight.mongo.setup.MongoSetupHelper;

@RunWith(JUnit4.class)
public class RecipeRepositoryTest {

	private static MongoClient mongo;
	private RecipeRepository recipeRepository;

	@BeforeClass
	public static void setupMongo() throws Exception {
		mongo = MongoSetupHelper.startMongoInstance();
	}

	@AfterClass
	public static void teardown() {
		MongoSetupHelper.cleanupMongo();
	}

	@Before
	public void setup() {
		recipeRepository = new RecipeRepository(mongo);
	}

	@Test
	public void simpleSaveAndGetWorks() throws Exception {
		final Recipe recipe = new Recipe("name", "content");

		final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);
		assertThat(savedRecipe.getId(), notNullValue());
		assertThat(savedRecipe.getName(), equalTo("name"));
		assertThat(savedRecipe.getContent(), equalTo("content"));

		final Recipe foundRecipe = recipeRepository.findRecipeById(savedRecipe.getId());
		assertThat(foundRecipe.getId(), equalTo(savedRecipe.getId()));
		assertThat(foundRecipe.getName(), equalTo("name"));
		assertThat(foundRecipe.getContent(), equalTo("content"));
	}

	@Test
	public void queryAllRecipesWorksAsExpected() throws Exception {
		final Recipe recipe1 = new Recipe("queryAllRecipesWorksAsExpected_name1", "queryAllRecipesWorksAsExpected_content1");
		final Recipe recipe2 = new Recipe("queryAllRecipesWorksAsExpected_name2", "queryAllRecipesWorksAsExpected_content2");
		final Recipe recipe3 = new Recipe("queryAllRecipesWorksAsExpected_name3", "queryAllRecipesWorksAsExpected_content3");

		recipeRepository.saveNewRecipe(recipe1);
		recipeRepository.saveNewRecipe(recipe2);
		recipeRepository.saveNewRecipe(recipe3);

		final List<Recipe> recipeList = recipeRepository.findAllRecipes();

		final Recipe foundRecipe1 = findRecipeByName("queryAllRecipesWorksAsExpected_name1", recipeList);
		assertThat(foundRecipe1.getContent(), equalTo("queryAllRecipesWorksAsExpected_content1"));

		final Recipe foundRecipe2 = findRecipeByName("queryAllRecipesWorksAsExpected_name2", recipeList);
		assertThat(foundRecipe2.getContent(), equalTo("queryAllRecipesWorksAsExpected_content2"));

		final Recipe foundRecipe3 = findRecipeByName("queryAllRecipesWorksAsExpected_name3", recipeList);
		assertThat(foundRecipe3.getContent(), equalTo("queryAllRecipesWorksAsExpected_content3"));
	}

	private Recipe findRecipeByName(final String nameToFind, final List<Recipe> recipeList) {
		for (final Recipe recipe : recipeList) {
			if (recipe.getName().equals(nameToFind)) {
				return recipe;
			}
		}
		return null;
	}

	@Test
	public void deleteWorks() throws Exception {
		final Recipe recipe1 = new Recipe("deleteWorks_name1", "deleteWorks_content1");
		final Recipe recipe2 = new Recipe("deleteWorks_name2", "deleteWorks_content2");
		final Recipe recipe3 = new Recipe("deleteWorks_name3", "deleteWorks_content3");

		final Recipe recipeToDelete = recipeRepository.saveNewRecipe(recipe1);
		recipeRepository.saveNewRecipe(recipe2);
		recipeRepository.saveNewRecipe(recipe3);

		final List<Recipe> recipeList = recipeRepository.findAllRecipes();
		final int initialSize = recipeList.size();
		assertThat(findRecipeByName("deleteWorks_name1", recipeList), notNullValue());
		assertThat(findRecipeByName("deleteWorks_name2", recipeList), notNullValue());
		assertThat(findRecipeByName("deleteWorks_name3", recipeList), notNullValue());

		recipeRepository.deleteRecipe(recipeToDelete.getId());

		final List<Recipe> recipeListAfterDelete = recipeRepository.findAllRecipes();
		assertThat(recipeListAfterDelete.size(), equalTo(initialSize - 1));

		assertThat(findRecipeByName("deleteWorks_name1", recipeListAfterDelete), nullValue());
		assertThat(findRecipeByName("deleteWorks_name2", recipeListAfterDelete), notNullValue());
		assertThat(findRecipeByName("deleteWorks_name3", recipeListAfterDelete), notNullValue());
	}
}
