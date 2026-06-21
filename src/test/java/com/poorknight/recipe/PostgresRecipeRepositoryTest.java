package com.poorknight.recipe;

import com.poorknight.test.setup.PostgresTestHelper;
import com.poorknight.recipe.Recipe.RecipeId;
import com.poorknight.recipe.exception.NoRecipeExistsForIdException;
import com.poorknight.recipe.search.SearchTag;
import org.assertj.core.api.Assertions;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class PostgresRecipeRepositoryTest {

    private RecipeRepository recipeRepository;
    private DataSource dataSource;


    @BeforeAll
    public static void setupDatabase() throws Exception {
        PostgresTestHelper.startPostgresAndMigrateTables();
    }

    @AfterAll
    public static void teardown() {
        PostgresTestHelper.stopPostgres();
    }

    @BeforeEach
    public void setup() {
        dataSource = PostgresTestHelper.buildDataSource();
        recipeRepository = new PostgresRecipeRepository(dataSource);
    }

    @AfterEach
    public void tearDown() throws Exception {
        PostgresTestHelper.deleteAllRecipes();
        ((AutoCloseable) dataSource).close();
    }

    @Test
    public void simpleSaveAndGet_Works() throws Exception {
        final Recipe recipe = new Recipe("name", "content", new Recipe.UserId("userId"), new RecipeImage("imageId", "imageUrl"));

        final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);
        assertThat(savedRecipe, notNullValue());
        assertThat(savedRecipe.getId(), notNullValue());
        assertThat(savedRecipe.getName(), equalTo("name"));
        assertThat(savedRecipe.getContent(), equalTo("content"));
        assertThat(savedRecipe.getOwningUserId().getValue(), equalTo("userId"));
        assertThat(savedRecipe.getImage().getImageId(), equalTo("imageId"));
        assertThat(savedRecipe.getImage().getImageUrl(), equalTo("imageUrl"));

        final Recipe foundRecipe = recipeRepository.findRecipeById(savedRecipe.getId());
        assertThat(foundRecipe, notNullValue());
        assertThat(foundRecipe.getId(), equalTo(savedRecipe.getId()));
        assertThat(foundRecipe.getName(), equalTo("name"));
        assertThat(foundRecipe.getContent(), equalTo("content"));
        assertThat(foundRecipe.getOwningUserId().getValue(), equalTo("userId"));
        assertThat(foundRecipe.getImage().getImageId(), equalTo("imageId"));
        assertThat(foundRecipe.getImage().getImageUrl(), equalTo("imageUrl"));
    }

    @Test
    public void saveAndGet_PreservesCategoryAndTags() throws Exception {
        final Recipe recipe = new Recipe(null, "name", "content", new Recipe.UserId("userId"), null, "Main Dish", Arrays.asList("Vegetarian", "Quick & Easy"));

        final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);
        Assertions.assertThat(savedRecipe.getCategory()).isEqualTo("Main Dish");
        Assertions.assertThat(savedRecipe.getTags()).containsExactlyInAnyOrder("Vegetarian", "Quick & Easy");

        final Recipe foundRecipe = recipeRepository.findRecipeById(savedRecipe.getId());
        Assertions.assertThat(foundRecipe.getCategory()).isEqualTo("Main Dish");
        Assertions.assertThat(foundRecipe.getTags()).containsExactlyInAnyOrder("Vegetarian", "Quick & Easy");
    }

    @Test
    public void saveAndGet_WithNoTagsAndNullCategory_Works() throws Exception {
        final Recipe recipe = new Recipe("name", "content", new Recipe.UserId("userId"));

        final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);
        final Recipe foundRecipe = recipeRepository.findRecipeById(savedRecipe.getId());

        Assertions.assertThat(foundRecipe.getCategory()).isNull();
        Assertions.assertThat(foundRecipe.getTags()).isEmpty();
    }

    @Test
    public void updateRecipe_CanChangeCategoryAndTags() throws Exception {
        final Recipe recipe = new Recipe(null, "name", "content", new Recipe.UserId("userId"), null, "Main Dish", Arrays.asList("Vegetarian", "Quick"));
        final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);

        final Recipe recipeToUpdate = new Recipe(savedRecipe.getId(), "name", "content", savedRecipe.getOwningUserId(), null, "Dessert", Arrays.asList("Vegan"));
        recipeRepository.updateRecipe(recipeToUpdate);

        final Recipe foundRecipe = recipeRepository.findRecipeById(savedRecipe.getId());
        Assertions.assertThat(foundRecipe.getCategory()).isEqualTo("Dessert");
        Assertions.assertThat(foundRecipe.getTags()).containsExactly("Vegan");
    }

    @Test
    public void deletingARecipe_CascadeDeletesItsTags() throws Exception {
        final Recipe recipe = new Recipe(null, "name", "content", new Recipe.UserId("userId"), null, "Main Dish", Arrays.asList("Vegetarian", "Quick"));
        final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);
        Assertions.assertThat(countTagRows(savedRecipe.getId().getValue())).isEqualTo(2);

        recipeRepository.deleteRecipe(savedRecipe.getId());

        Assertions.assertThat(countTagRows(savedRecipe.getId().getValue())).isEqualTo(0);
    }

    @Test
    public void rateRecipe_StoresAndAggregatesARating() throws Exception {
        final Recipe saved = recipeRepository.saveNewRecipe(new Recipe("name", "content", new Recipe.UserId("owner")));

        final Recipe rated = recipeRepository.rateRecipe(saved.getId(), new Recipe.UserId("rater1"), 5);
        Assertions.assertThat(rated.getRating().getAverage()).isEqualTo(5.0);
        Assertions.assertThat(rated.getRating().getCount()).isEqualTo(1);

        final Recipe found = recipeRepository.findRecipeById(saved.getId());
        Assertions.assertThat(found.getRating().getAverage()).isEqualTo(5.0);
        Assertions.assertThat(found.getRating().getCount()).isEqualTo(1);
    }

    @Test
    public void rateRecipe_AveragesAcrossMultipleUsers() throws Exception {
        final Recipe saved = recipeRepository.saveNewRecipe(new Recipe("name", "content", new Recipe.UserId("owner")));

        recipeRepository.rateRecipe(saved.getId(), new Recipe.UserId("rater1"), 5);
        final Recipe rated = recipeRepository.rateRecipe(saved.getId(), new Recipe.UserId("rater2"), 4);

        Assertions.assertThat(rated.getRating().getAverage()).isEqualTo(4.5);
        Assertions.assertThat(rated.getRating().getCount()).isEqualTo(2);
    }

    @Test
    public void rateRecipe_SameUserReRating_UpdatesInsteadOfAddingARow() throws Exception {
        final Recipe saved = recipeRepository.saveNewRecipe(new Recipe("name", "content", new Recipe.UserId("owner")));

        recipeRepository.rateRecipe(saved.getId(), new Recipe.UserId("rater1"), 2);
        final Recipe rated = recipeRepository.rateRecipe(saved.getId(), new Recipe.UserId("rater1"), 5);

        Assertions.assertThat(rated.getRating().getAverage()).isEqualTo(5.0);
        Assertions.assertThat(rated.getRating().getCount()).isEqualTo(1);
    }

    @Test
    public void findRecipe_WithNoRatings_ReturnsZeroAggregate() throws Exception {
        final Recipe saved = recipeRepository.saveNewRecipe(new Recipe("name", "content", new Recipe.UserId("owner")));

        final Recipe found = recipeRepository.findRecipeById(saved.getId());

        Assertions.assertThat(found.getRating().getAverage()).isEqualTo(0.0);
        Assertions.assertThat(found.getRating().getCount()).isEqualTo(0);
    }

    @Test
    public void rateRecipe_ForMissingRecipe_ThrowsNoRecipeExists() throws Exception {
        final RecipeId missingId = new RecipeId(RandomStringUtils.randomAlphanumeric(24));
        org.junit.jupiter.api.Assertions.assertThrows(NoRecipeExistsForIdException.class,
                () -> recipeRepository.rateRecipe(missingId, new Recipe.UserId("rater1"), 5));
    }

    @Test
    public void deletingARecipe_CascadeDeletesItsRatings() throws Exception {
        final Recipe saved = recipeRepository.saveNewRecipe(new Recipe("name", "content", new Recipe.UserId("owner")));
        recipeRepository.rateRecipe(saved.getId(), new Recipe.UserId("rater1"), 5);
        Assertions.assertThat(countRatingRows(saved.getId().getValue())).isEqualTo(1);

        recipeRepository.deleteRecipe(saved.getId());

        Assertions.assertThat(countRatingRows(saved.getId().getValue())).isEqualTo(0);
    }

    @Test
    public void suggestTagsForCategory_ordersByUsageThenAlpha_scopedToCategory() throws Exception {
        recipeRepository.saveNewRecipe(new Recipe(null, "r1", "c", new Recipe.UserId("u"), null, "Main Dish", Arrays.asList("Vegetarian", "Quick")));
        recipeRepository.saveNewRecipe(new Recipe(null, "r2", "c", new Recipe.UserId("u"), null, "Main Dish", Arrays.asList("Vegetarian")));
        recipeRepository.saveNewRecipe(new Recipe(null, "r3", "c", new Recipe.UserId("u"), null, "Main Dish", Arrays.asList("Spicy")));
        recipeRepository.saveNewRecipe(new Recipe(null, "r4", "c", new Recipe.UserId("u"), null, "Dessert", Arrays.asList("Vegetarian")));

        final List<String> suggestions = recipeRepository.suggestTagsForCategory("Main Dish", 24);

        // Vegetarian used twice → first; the count-1 tags follow alphabetically. Dessert's tag is excluded.
        Assertions.assertThat(suggestions).containsExactly("Vegetarian", "Quick", "Spicy");
    }

    @Test
    public void suggestTagsForCategory_respectsLimit() throws Exception {
        recipeRepository.saveNewRecipe(new Recipe(null, "r1", "c", new Recipe.UserId("u"), null, "Main Dish", Arrays.asList("Vegetarian", "Quick")));
        recipeRepository.saveNewRecipe(new Recipe(null, "r2", "c", new Recipe.UserId("u"), null, "Main Dish", Arrays.asList("Vegetarian")));

        final List<String> suggestions = recipeRepository.suggestTagsForCategory("Main Dish", 1);

        Assertions.assertThat(suggestions).containsExactly("Vegetarian");
    }

    @Test
    public void suggestTagsForCategory_unknownCategory_returnsEmpty() throws Exception {
        recipeRepository.saveNewRecipe(new Recipe(null, "r1", "c", new Recipe.UserId("u"), null, "Main Dish", Arrays.asList("Vegetarian")));

        final List<String> suggestions = recipeRepository.suggestTagsForCategory("Nonexistent", 24);

        Assertions.assertThat(suggestions).isEmpty();
    }

    @Test
    public void getRecipe_WhereNoneExists_ReturnsNull() throws Exception {
        final String validMongoId = RandomStringUtils.randomAlphanumeric(24);
        final Recipe recipe = recipeRepository.findRecipeById(new RecipeId(validMongoId));
        assertThat(recipe, nullValue());
    }

    @Test
    public void getRecipe_WithInvalidMongoId_ReturnsNull() throws Exception {
        final String invalidMongoId = "hi";
        final Recipe recipe = recipeRepository.findRecipeById(new RecipeId(invalidMongoId));
        assertThat(recipe, nullValue());
    }

    @Test
    public void saveNewRecipe_WithAnExistingId_ThrowsException() throws Exception {
        try {
            final Recipe recipe = new Recipe(new RecipeId("id"), "name", "content", new Recipe.UserId("userId"));
            recipeRepository.saveNewRecipe(recipe);
            fail("expecting exception");
        } catch (final RuntimeException e) {
            assertThat(e.getMessage(), equalTo("Only new recipes can be saved in this way.  There should not be a RecipeId, but one was found: id"));
        }
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void queryAllRecipes_WorksAsExpected() throws Exception {
        final Recipe recipe1 = new Recipe("queryAllRecipesWorksAsExpected_name1", "queryAllRecipesWorksAsExpected_content1", new Recipe.UserId("userId1"));
        final Recipe recipe2 = new Recipe("queryAllRecipesWorksAsExpected_name2", "queryAllRecipesWorksAsExpected_content2", new Recipe.UserId("userId2"));
        final Recipe recipe3 = new Recipe("queryAllRecipesWorksAsExpected_name3", "queryAllRecipesWorksAsExpected_content3", new Recipe.UserId("userId3"));

        saveRecipes(recipe1, recipe2, recipe3);

        final List<Recipe> recipeList = recipeRepository.findAllRecipes();

        final Recipe foundRecipe1 = findRecipeByName("queryAllRecipesWorksAsExpected_name1", recipeList);
        assertThat(foundRecipe1.getContent(), equalTo("queryAllRecipesWorksAsExpected_content1"));
        assertThat(foundRecipe1.getOwningUserId().getValue(), equalTo("userId1"));

        final Recipe foundRecipe2 = findRecipeByName("queryAllRecipesWorksAsExpected_name2", recipeList);
        assertThat(foundRecipe2.getContent(), equalTo("queryAllRecipesWorksAsExpected_content2"));
        assertThat(foundRecipe2.getOwningUserId().getValue(), equalTo("userId2"));

        final Recipe foundRecipe3 = findRecipeByName("queryAllRecipesWorksAsExpected_name3", recipeList);
        assertThat(foundRecipe3.getContent(), equalTo("queryAllRecipesWorksAsExpected_content3"));
        assertThat(foundRecipe3.getOwningUserId().getValue(), equalTo("userId3"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getRecipesById_WorksAsExpected() throws Exception {
        final Recipe recipe1 = new Recipe("getRecipesById_name1", "content1", new Recipe.UserId("userId1"));
        final Recipe recipe2 = new Recipe("getRecipesById_name2", "content2", new Recipe.UserId("userId2"));
        final Recipe recipe3 = new Recipe("getRecipesById_name3", "content3", new Recipe.UserId("userId3"));

        final RecipeId id1 = recipeRepository.saveNewRecipe(recipe1).getId();
        recipeRepository.saveNewRecipe(recipe2);
        final RecipeId id3 = recipeRepository.saveNewRecipe(recipe3).getId();

        final List<RecipeId> recipeIdsToFind = Arrays.asList(id1, id3);
        final List<Recipe> recipeList = recipeRepository.findRecipesWithIds(recipeIdsToFind);

        Assertions.assertThat(recipeList.size()).isEqualTo(2);

        final Recipe foundRecipe1 = findRecipeByName("getRecipesById_name1", recipeList);
        assertThat(foundRecipe1.getContent(), equalTo("content1"));

        final Recipe foundRecipe2 = findRecipeByName("getRecipesById_name3", recipeList);
        assertThat(foundRecipe2.getContent(), equalTo("content3"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getRecipesById_WithInvalidOrNullOrUnknownIds_JustIgnoresThem() throws Exception {
        final Recipe recipe1 = new Recipe("getRecipesById_ExtraIds_name1", "content1", new Recipe.UserId("userId1"));
        final Recipe recipe2 = new Recipe("getRecipesById_ExtraIds_name2", "content2", new Recipe.UserId("userId2"));
        final Recipe recipe3 = new Recipe("getRecipesById_ExtraIds_name3", "content3", new Recipe.UserId("userId3"));

        final RecipeId id1 = recipeRepository.saveNewRecipe(recipe1).getId();
        recipeRepository.saveNewRecipe(recipe2);
        final RecipeId id3 = recipeRepository.saveNewRecipe(recipe3).getId();

        final RecipeId nullId = null;
        final RecipeId nullValueId = new RecipeId(null);
        final RecipeId emptyValueId = new RecipeId("");
        final RecipeId invalidId = new RecipeId("invalid");
        final RecipeId validUnknownId = new RecipeId(RandomStringUtils.randomAlphanumeric(24));

        final List<RecipeId> recipeIdsToFind = Arrays.asList(id1, id3, nullId, nullValueId, emptyValueId, invalidId, validUnknownId);
        final List<Recipe> recipeList = recipeRepository.findRecipesWithIds(recipeIdsToFind);

        Assertions.assertThat(recipeList.size()).isEqualTo(2);

        final Recipe foundRecipe1 = findRecipeByName("getRecipesById_ExtraIds_name1", recipeList);
        assertThat(foundRecipe1.getContent(), equalTo("content1"));

        final Recipe foundRecipe2 = findRecipeByName("getRecipesById_ExtraIds_name3", recipeList);
        assertThat(foundRecipe2.getContent(), equalTo("content3"));
    }

    @Test
    public void searchRecipes_WithSearchTags_FindsByName() {
        final Recipe recipe1 = new Recipe("searchName1 findMe1", "searchContent1", new Recipe.UserId("userId"));
        final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe2", new Recipe.UserId("userId"));
        final Recipe recipe3 = new Recipe("searchName3", "searchContent3", new Recipe.UserId("userId"));

        saveRecipes(recipe1, recipe2, recipe3);

        final List<SearchTag> searchTags = Collections.singletonList(new SearchTag("findMe1"));
        final List<Recipe> foundRecipes = recipeRepository.searchRecipes(searchTags);

        assertThat(foundRecipes.size(), equalTo(1));
        assertThat(foundRecipes.get(0).getName(), equalTo("searchName1 findMe1"));
    }

    @Test
    public void searchRecipes_WithSearchTags_FindsByContent() {
        final Recipe recipe1 = new Recipe("searchName1 findMe1", "searchContent1", new Recipe.UserId("userId"));
        final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe2", new Recipe.UserId("userId"));
        final Recipe recipe3 = new Recipe("searchName3", "searchContent3", new Recipe.UserId("userId"));

        saveRecipes(recipe1, recipe2, recipe3);

        final List<SearchTag> searchTags = Collections.singletonList(new SearchTag("findMe2"));
        final List<Recipe> foundRecipes = recipeRepository.searchRecipes(searchTags);

        assertThat(foundRecipes.size(), equalTo(1));
        assertThat(foundRecipes.get(0).getName(), equalTo("searchName2"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void searchRecipes_WithSearchTags_FindsByBothNameAndContent() {
        final Recipe recipe1 = new Recipe("searchName1 findMe", "searchContent1", new Recipe.UserId("userId"));
        final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe", new Recipe.UserId("userId"));
        final Recipe recipe3 = new Recipe("searchName3", "searchContent3", new Recipe.UserId("userId"));

        saveRecipes(recipe1, recipe2, recipe3);

        final List<SearchTag> searchTags = Collections.singletonList(new SearchTag("findMe"));
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
    @SuppressWarnings("ConstantConditions")
    public void searchRecipes_WithMultipleSearchTags_ReturnsAResultIfAnyTagIsFound() {
        final Recipe recipe1 = new Recipe("searchName1 findMe", "searchContent1", new Recipe.UserId("userId"));
        final Recipe recipe2 = new Recipe("searchName2", "searchContent2 findMe", new Recipe.UserId("userId"));
        final Recipe recipe3 = new Recipe("searchName3", "searchContent3", new Recipe.UserId("userId"));

        saveRecipes(recipe1, recipe2, recipe3);

        final List<SearchTag> searchTags = Arrays.asList(new SearchTag("notHere"), new SearchTag("Again"), new SearchTag("FINDme"));
        final List<Recipe> foundRecipes = recipeRepository.searchRecipes(searchTags);

        assertThat(foundRecipes.size(), equalTo(2));

        final Recipe foundRecipe1 = findRecipeByName("searchName1 findMe", foundRecipes);
        assertThat(foundRecipe1.getContent(), equalTo("searchContent1"));

        final Recipe foundRecipe2 = findRecipeByName("searchName2", foundRecipes);
        assertThat(foundRecipe2.getContent(), equalTo("searchContent2 findMe"));
    }

    @Test
    public void updateRecipe_CanChangeNameAndContentAndImageUrl() throws Exception {
        final Recipe recipe = new Recipe("originalName", "originalContent", new Recipe.UserId("userId"), new RecipeImage("originalImageId", "originalImageUrl"));
        final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);

        Recipe recipeToUseForUpdate = new Recipe(savedRecipe.getId(), "updatedName", "updatedContent", savedRecipe.getOwningUserId(), new RecipeImage("updatedImageId", "updatedImageUrl"));
        final Recipe updatedRecipe = recipeRepository.updateRecipe(recipeToUseForUpdate);

        Assertions.assertThat(updatedRecipe.getId()).isEqualTo(savedRecipe.getId());
        Assertions.assertThat(updatedRecipe.getName()).isEqualTo("updatedName");
        Assertions.assertThat(updatedRecipe.getContent()).isEqualTo("updatedContent");
        Assertions.assertThat(updatedRecipe.getOwningUserId()).isEqualTo(savedRecipe.getOwningUserId());
        Assertions.assertThat(updatedRecipe.getImage().getImageId()).isEqualTo("updatedImageId");
        Assertions.assertThat(updatedRecipe.getImage().getImageUrl()).isEqualTo("updatedImageUrl");

        final Recipe foundRecipe = recipeRepository.findRecipeById(recipeToUseForUpdate.getId());
        Assertions.assertThat(foundRecipe.getId()).isEqualTo(savedRecipe.getId());
        Assertions.assertThat(foundRecipe.getName()).isEqualTo("updatedName");
        Assertions.assertThat(foundRecipe.getContent()).isEqualTo("updatedContent");
        Assertions.assertThat(foundRecipe.getOwningUserId()).isEqualTo(savedRecipe.getOwningUserId());
        Assertions.assertThat(foundRecipe.getImage().getImageId()).isEqualTo("updatedImageId");
        Assertions.assertThat(foundRecipe.getImage().getImageUrl()).isEqualTo("updatedImageUrl");
    }

    @Test
    public void updateRecipe_DoesNotChangeUser() throws Exception {
        final Recipe recipe = new Recipe("originalName", "originalContent", new Recipe.UserId("userId"));
        final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipe);

        Recipe recipeToUseForUpdate = new Recipe(savedRecipe.getId(), "updatedName", "updatedContent", new Recipe.UserId("newUserId"));
        final Recipe updatedRecipe = recipeRepository.updateRecipe(recipeToUseForUpdate);

        Assertions.assertThat(updatedRecipe.getId()).isEqualTo(savedRecipe.getId());
        Assertions.assertThat(updatedRecipe.getName()).isEqualTo("updatedName");
        Assertions.assertThat(updatedRecipe.getContent()).isEqualTo("updatedContent");
        Assertions.assertThat(updatedRecipe.getOwningUserId()).isEqualTo(new Recipe.UserId("userId"));

        final Recipe foundRecipe = recipeRepository.findRecipeById(recipeToUseForUpdate.getId());
        Assertions.assertThat(foundRecipe.getId()).isEqualTo(savedRecipe.getId());
        Assertions.assertThat(foundRecipe.getName()).isEqualTo("updatedName");
        Assertions.assertThat(foundRecipe.getContent()).isEqualTo("updatedContent");
        Assertions.assertThat(foundRecipe.getOwningUserId()).isEqualTo(new Recipe.UserId("userId"));
    }

    @Test
    public void updateRecipe_ThrowsException_IfNoRecipeExistsForGivenId() throws Exception {
        try {
            final Recipe recipeToUseForUpdate = new Recipe(new RecipeId("576b1d15a7c0a00de7193085"), "originalName", "originalContent", new Recipe.UserId("userId"));
            recipeRepository.updateRecipe(recipeToUseForUpdate);
            fail("expected exception");
        } catch (NoRecipeExistsForIdException e) {
            Assertions.assertThat(e.getMessage()).isEqualTo("Cannot update recipe - no recipe found with id: 576b1d15a7c0a00de7193085");
        }
    }

    @Test
    public void updateRecipe_ThrowsException_IfNoRecipeExistsForMalformedId() throws Exception {
        try {
            final Recipe recipeToUseForUpdate = new Recipe(new RecipeId("hi"), "originalName", "originalContent", new Recipe.UserId("userId"));
            recipeRepository.updateRecipe(recipeToUseForUpdate);
            fail("expected exception");
        } catch (NoRecipeExistsForIdException e) {
            Assertions.assertThat(e.getMessage()).isEqualTo("Cannot update recipe - no recipe found with id: hi");
        }
    }

    @Test
    public void updateRecipe_ThrowsException_ForNullId() throws Exception {
        try {
            final Recipe recipeToUseForUpdate = new Recipe(null, "originalName", "originalContent", new Recipe.UserId("userId"));
            recipeRepository.updateRecipe(recipeToUseForUpdate);
            fail("expected exception");
        } catch (NoRecipeExistsForIdException e) {
            Assertions.assertThat(e.getMessage()).isEqualTo("Cannot update recipe - no recipe found with id: null");
        }
    }

    @Test
    public void deleteWorks() throws Exception {
        final Recipe recipe1 = new Recipe("deleteWorks_name1", "deleteWorks_content1", new Recipe.UserId("userId"));
        final Recipe recipe2 = new Recipe("deleteWorks_name2", "deleteWorks_content2", new Recipe.UserId("userId"));
        final Recipe recipe3 = new Recipe("deleteWorks_name3", "deleteWorks_content3", new Recipe.UserId("userId"));

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

    private int countTagRows(final String recipeId) throws Exception {
        final DataSource dataSource = PostgresTestHelper.buildDataSource();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT count(*) FROM recipe_tag WHERE recipe_id = ?");
            statement.setString(1, recipeId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } finally {
            ((AutoCloseable) dataSource).close();
        }
    }

    private int countRatingRows(final String recipeId) throws Exception {
        final DataSource dataSource = PostgresTestHelper.buildDataSource();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT count(*) FROM recipe_rating WHERE recipe_id = ?");
            statement.setString(1, recipeId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        } finally {
            ((AutoCloseable) dataSource).close();
        }
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
