package com.poorknight.recipebook;

public interface RecipeBookRepository {
    RecipeBook getRecipeBook(RecipeBook.UserId userId);

    RecipeBook.RecipeId addRecipeIdToRecipeBook(RecipeBook.UserId userId, RecipeBook.RecipeId recipeId);

    void deleteRecipeFromRecipeBook(RecipeBook.UserId userId, RecipeBook.RecipeId recipeId);
}
