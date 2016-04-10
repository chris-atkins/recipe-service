package com.poorknight.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
}
