package com.poorknight.recipe;

import com.poorknight.recipe.search.SearchTag;

import java.util.List;

public interface RecipeRepository {
    Recipe saveNewRecipe(Recipe recipe);

    Recipe updateRecipe(Recipe recipeToUpdate);

    Recipe findRecipeById(Recipe.RecipeId id);

    List<Recipe> findAllRecipes();

    List<Recipe> findRecipesWithIds(List<Recipe.RecipeId> recipeIdsToFind);

    List<Recipe> searchRecipes(List<SearchTag> searchTags);

    List<String> suggestTagsForCategory(String category, int limit);

    Recipe rateRecipe(Recipe.RecipeId recipeId, Recipe.UserId userId, int rating);

    Recipe addTag(Recipe.RecipeId recipeId, String tag, Recipe.UserId userId);

    boolean removeTag(Recipe.RecipeId recipeId, String tag, Recipe.UserId userId);

    List<String> findTagsAddedByUser(Recipe.RecipeId recipeId, Recipe.UserId userId);

    void deleteRecipe(Recipe.RecipeId id);
}
