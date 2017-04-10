package com.poorknight.api;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.codahale.metrics.annotation.Timed;
import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.RecipeImage;
import com.poorknight.recipe.RecipeRepository;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isEmpty;


@Path("/recipe")
public class RecipeImageEndpoint {

	private static final String BUCKET_NAME = "myrecipeconnection.com.images";

	private final RecipeRepository recipeRepository;

	public RecipeImageEndpoint(final RecipeRepository recipeRepository){
		this.recipeRepository = recipeRepository;
	}

	@POST
	@Timed(name = "postRecipeImage")
	@Path("/{id}/image")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postRecipeImage(@PathParam("id") final String recipeId, @FormDataParam("file") final InputStream imageInputStream, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final Recipe recipe = demandRecipe(recipeId);
		validateRequestingUserHasPermission(requestingUserIdString, recipe, "upload");

		final String imageId = UUID.randomUUID().toString();
		final URL url = uploadImageToS3(imageInputStream, imageId);
		final String imageUrl = makeUrlHttp(url);
		updateRecipe(recipe, new RecipeImage(imageId, imageUrl));

		return Response.ok(new RecipeImage(imageId, imageUrl)).build();
//		return makeCORS(Response.ok(recipeImage);
	}

	@DELETE
	@Timed(name = "deleteRecipeImage")
	@Path("/{recipeId}/image/{imageId}")
	public void deleteRecipeImage(@PathParam("recipeId") final String recipeId, @PathParam("imageId") final String imageId, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final Recipe recipe = demandRecipe(recipeId);
		validateRequestingUserHasPermission(requestingUserIdString, recipe, "delete");
		validateImageExists(imageId, recipe);

		deleteImageFromS3(imageId);
		updateRecipe(recipe, null);
	}

	private Recipe demandRecipe(final String recipeId) {
		final Recipe recipe = recipeRepository.findRecipeById(new Recipe.RecipeId(recipeId));
		if (recipe == null) {
			throw new WebApplicationException("No recipe exists for the id: " + recipeId, 404);
		}
		return recipe;
	}

	private void updateRecipe(final Recipe recipe, final RecipeImage image) {
		final Recipe updatedRecipe = new Recipe(recipe.getId(), recipe.getName(), recipe.getContent(), recipe.getOwningUserId(), image);
		recipeRepository.updateRecipe(updatedRecipe);
	}

	private URL uploadImageToS3(final @FormDataParam("file") InputStream imageInputStream, final String imageId) {
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		s3.putObject(buildS3PutImageRequest(imageInputStream, imageId));
		return s3.getUrl(BUCKET_NAME, imageId);
	}

	private PutObjectRequest buildS3PutImageRequest(final InputStream imageInputStream, final String key) {
		final PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key, imageInputStream, new ObjectMetadata());
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		return request;
	}

	private void deleteImageFromS3(final String imageId) {
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
		s3.deleteObject(BUCKET_NAME, imageId);
	}

	private String makeUrlHttp(final URL url) {
		final String original = url.toExternalForm();
		return StringUtils.replace(original, "https:", "http:");
	}

	private void validateRequestingUserHasPermission(final @HeaderParam("RequestingUser") String requestingUserIdString, final Recipe recipe, final String action) {
		if(isEmpty(requestingUserIdString)) {
			throw new WebApplicationException("You must be an authenticated user in order to attempt to " + action +" an image.", 401);
		}
		if(!recipe.getOwningUserId().getValue().equals(requestingUserIdString)) {
			throw new WebApplicationException("Only the recipe owner may " + action + " an image for that recipe.", 401);
		}
	}

	private void validateImageExists(final String imageId, final Recipe recipe) {
		if(recipe.getImage() == null || !recipe.getImage().getImageId().equals(imageId)) {
			throw new WebApplicationException("No image exists for the id: " + imageId, 404);
		}
	}

//	private String _corsHeaders;
//	private final Logger logger = LoggerFactory.getLogger(getClass());

//	@OPTIONS
//	@Path("/{id}/image")
//	public Response myResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
//		_corsHeaders = requestH;
//		return makeCORS(Response.ok(), requestH);
//	}
//
//	private Response makeCORS(Response.ResponseBuilder req, String returnMethod) {
//		Response.ResponseBuilder rb = req.header("Access-Control-Allow-Origin", "*").header(
//				"Access-Control-Allow-Methods",
//				"GET, POST, OPTIONS");
//
//		if (!"".equals(returnMethod)) {
//			rb.header("Access-Control-Allow-Headers", returnMethod);
//		}
//
//		final Response response = rb.build();
//		logger.error(response.toString());
//		return response;
//	}
//
//	private Response makeCORS(Response.ResponseBuilder req) {
//		return makeCORS(req, _corsHeaders);
//	}

}