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

    void deleteRecipe(Recipe.RecipeId id);
}
