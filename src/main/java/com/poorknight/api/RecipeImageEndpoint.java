package com.poorknight.api;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.codahale.metrics.annotation.Timed;
import com.poorknight.recipe.RecipeImage;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;


@Path("/recipe")
public class RecipeImageEndpoint {

	private static final String BUCKET_NAME = "myrecipeconnection.com.images";

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

	@POST
	@Timed(name = "putRecipe")
	@Path("/{id}/image")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postRecipeImage(@FormDataParam("file") InputStream imageInputStream) {
		final String imageId = UUID.randomUUID().toString();
		final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

		s3.putObject(buildPutImageRequest(imageInputStream, imageId));
		final URL url = s3.getUrl(BUCKET_NAME, imageId);

		final RecipeImage recipeImage = new RecipeImage(imageId, makeUrlHttp(url));
		return Response.ok(recipeImage).build();
//		return makeCORS(Response.ok(recipeImage);
	}

	private PutObjectRequest buildPutImageRequest(final InputStream imageInputStream, final String key) {
		final PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key, imageInputStream, new ObjectMetadata());
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		return request;
	}

	private String makeUrlHttp(final URL url) {
		final String original = url.toExternalForm();
		return StringUtils.replace(original, "https:", "http:");
	}
}