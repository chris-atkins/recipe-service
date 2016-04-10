package com.richo.test.dropwizard.api;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.poorknight.domain.Recipe;

@Path("/recipe")
public interface RecipeApi {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	List<Recipe> getRecipes(@QueryParam("searchString") final String searchString);

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	Recipe postRecipe(final Recipe recipe);

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	Recipe getRecipe(@PathParam("id") final Long recipeId);

	@DELETE
	@Path("/{id}")
	void deleteRecipe(@PathParam("id") final Long recipeId);
}
