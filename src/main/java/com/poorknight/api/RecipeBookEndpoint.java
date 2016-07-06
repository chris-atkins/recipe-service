package com.poorknight.api;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.recipebook.ApiRecipeBook.ApiRecipeId;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/user/{userId}/recipe-book")
public class RecipeBookEndpoint {

    @POST
    @Path("/")
    @Timed(name = "postIdToRecipeBook")
    public ApiRecipeId postIdToRecipeBook(@PathParam("userId") final String userId, final ApiRecipeId recipeId) {
		return new ApiRecipeId(recipeId.getRecipeId());
	}
}
