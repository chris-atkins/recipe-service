package com.poorknight.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.mongodb.MongoClient;
import com.poorknight.domain.Recipe;
import com.poorknight.mongo.setup.MongoSetupHelper;
import com.richo.test.dropwizard.api.SearchTag;

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

	@After
	public void tearDown() {
		MongoSetupHelper.deleteAllRecipes();
	}

	@Test
	public void simpleSaveAndGet_Works() throws Exception {
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
	public void queryAllRecipes_WorksAsExpected() throws Exception {
		final Recipe recipe1 = new Recipe("queryAllRecipesWorksAsExpected_name1", "queryAllRecipesWorksAsExpected_content1");
		final Recipe recipe2 = new Recipe("queryAllRecipesWorksAsExpected_name2", "queryAllRecipesWorksAsExpected_content2");
		final Recipe recipe3 = new Recipe("queryAllRecipesWorksAsExpected_name3", "queryAllRecipesWorksAsExpected_content3");

		saveRecipes(recipe1, recipe2, recipe3);

		final List<Recipe> recipeList = recipeRepository.findAllRecipes();

		final Recipe foundRecipe1 = findRecipeByName("queryAllRecipesWorksAsExpected_name1", recipeList);
		assertThat(foundRecipe1.getContent(), equalTo("queryAllRecipesWorksAsExpected_content1"));

		final Recipe foundRecipe2 = findRecipeByName("queryAllRecipesWorksAsExpected_name2", recipeList);
		assertThat(foundRecipe2.getContent(), equalTo("queryAllRecipesWorksAsExpected_content2"));

		final Recipe foundRecipe3 = findRecipeByName("queryAllRecipesWorksAsExpected_name3", recipeList);
		assertThat(foundRecipe3.getContent(), equalTo("queryAllRecipesWorksAsExpected_content3"));
	}

	@Test
	public void searchRecipes_WithSearchTags_FindsByName() {
		final Recipe recipe1 = new Recipe("searchName1 findMe1", "searchContent1");
		final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe2");
		final Recipe recipe3 = new Recipe("searchName3", "searchContent3");

		saveRecipes(recipe1, recipe2, recipe3);

		final List<SearchTag> searchTags = Arrays.asList(new SearchTag("findMe1"));
		final List<Recipe> foundRecipes = recipeRepository.searchRecipes(searchTags);

		assertThat(foundRecipes.size(), equalTo(1));
		assertThat(foundRecipes.get(0).getName(), equalTo("searchName1 findMe1"));
	}

	@Test
	public void searchRecipes_WithSearchTags_FindsByContent() {
		final Recipe recipe1 = new Recipe("searchName1 findMe1", "searchContent1");
		final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe2");
		final Recipe recipe3 = new Recipe("searchName3", "searchContent3");

		saveRecipes(recipe1, recipe2, recipe3);

		final List<SearchTag> searchTags = Arrays.asList(new SearchTag("findMe2"));
		final List<Recipe> foundRecipes = recipeRepository.searchRecipes(searchTags);

		assertThat(foundRecipes.size(), equalTo(1));
		assertThat(foundRecipes.get(0).getName(), equalTo("searchName2"));
	}

	@Test
	public void searchRecipes_WithSearchTags_FindsByBothNameAndContent() {
		final Recipe recipe1 = new Recipe("searchName1 findMe", "searchContent1");
		final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe");
		final Recipe recipe3 = new Recipe("searchName3", "searchContent3");

		saveRecipes(recipe1, recipe2, recipe3);

		final List<SearchTag> searchTags = Arrays.asList(new SearchTag("findMe"));
		final List<Recipe> foundRecipes = recipeRepository.searchRecipes(searchTags);

		final List<Recipe> allRecipes = recipeRepository.findAllRecipes();
		assertThat(allRecipes.size(), equalTo(3));

		assertThat(foundRecipes.size(), equalTo(2));

		final Recipe foundRecipe1 = findRecipeByName("searchName1 findMe", foundRecipes);
		assertThat(foundRecipe1.getContent(), equalTo("searchContent1"));

		final Recipe foundRecipe2 = findRecipeByName("searchName2", foundRecipes);
		assertThat(foundRecipe2.getContent(), equalTo("searchContent2 findMe"));
	}

	@Test
	public void searchRecipes_WithMultipleSearchTags_ReturnsAResultIfAnyTagIsFound() {
		final Recipe recipe1 = new Recipe("searchName1 findMe", "searchContent1");
		final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe");
		final Recipe recipe3 = new Recipe("searchName3", "searchContent3");

		saveRecipes(recipe1, recipe2, recipe3);

		final List<SearchTag> searchTags = Arrays.asList(new SearchTag("notHere"), new SearchTag("Again"), new SearchTag("findMe"));
		final List<Recipe> foundRecipes = recipeRepository.searchRecipes(searchTags);

		assertThat(foundRecipes.size(), equalTo(2));

		final Recipe foundRecipe1 = findRecipeByName("searchName1 findMe", foundRecipes);
		assertThat(foundRecipe1.getContent(), equalTo("searchContent1"));

		final Recipe foundRecipe2 = findRecipeByName("searchName2", foundRecipes);
		assertThat(foundRecipe2.getContent(), equalTo("searchContent2 findMe"));
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

	private void saveRecipes(final Recipe... recipes) {
		for (final Recipe recipe : recipes) {
			recipeRepository.saveNewRecipe(recipe);
		}
	}

	private Recipe findRecipeByName(final String nameToFind, final List<Recipe> recipeList) {
		for (final Recipe recipe : recipeList) {
			if (recipe.getName().equals(nameToFind)) {
				return recipe;
			}
		}
		return null;
	}
}
