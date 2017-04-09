package com.poorknight.api;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.RecipeImage;
import com.poorknight.recipe.RecipeRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonS3ClientBuilder.class, UUID.class, RecipeImageEndpoint.class})
public class RecipeImageEndpointTest {

	private static final String HTTPS_URL = "https://helloThere";
	private static final String HTTP_ONLY_URL = "http://helloThere";

	private UUID uuid;
	private String bucketName;
	private String key;
	private AmazonS3 s3;
	private InputStream imageInputStream;
	private Recipe recipeBeforeImage;
	private String recipeId;

	@InjectMocks
	private RecipeImageEndpoint endpoint;

	@Mock
	private RecipeRepository repository;

	@Captor
	private ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor;

	@Captor
	private ArgumentCaptor<Recipe> recipeCaptor;

	@Before
	public void setUp() throws Exception {
		uuid = UUID.randomUUID();
		recipeId = RandomStringUtils.random(20);
		bucketName = "myrecipeconnection.com.images";
		key = uuid.toString();
		s3 = Mockito.mock(AmazonS3.class);
		imageInputStream = new ByteArrayInputStream("image".getBytes());

		PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
		PowerMockito.mockStatic(UUID.class);

		when(UUID.randomUUID()).thenReturn(uuid);
		when(AmazonS3ClientBuilder.defaultClient()).thenReturn(s3);
		when(s3.getUrl(bucketName, key)).thenReturn(new URL(HTTPS_URL));

		Recipe recipeFromRepository = buildRecipeWithImageUrl(null);
		when(repository.findRecipeById(new Recipe.RecipeId(recipeId))).thenReturn(recipeFromRepository);
	}

	@Test
	public void endpointGeneratesAnIdForTheImageAndStoresItToAWSWWhileReplacingURLWithHttp() throws Exception {
		Response postImageResponse = endpoint.postRecipeImage(recipeId, imageInputStream);

		verify(s3).putObject(putObjectRequestArgumentCaptor.capture());
		final PutObjectRequest s3RequestObject = putObjectRequestArgumentCaptor.getValue();
		assertThat(s3RequestObject.getBucketName()).isEqualTo(bucketName);
		assertThat(s3RequestObject.getKey()).isEqualTo(key);
		assertThat(s3RequestObject.getInputStream()).isEqualTo(imageInputStream);
		assertThat(s3RequestObject.getCannedAcl()).isEqualTo(CannedAccessControlList.PublicRead);

		final RecipeImage imageResponse = (RecipeImage) postImageResponse.getEntity();
		assertThat(imageResponse.getImageId()).isEqualTo(key);
		assertThat(imageResponse.getImageUrl()).isEqualTo(HTTP_ONLY_URL);
	}

	@Test
	public void imageUrlIsSavedToTheRecipe() throws Exception {
		Recipe expectedRecipeBeingSaved = buildRecipeWithImageUrl(HTTP_ONLY_URL);

		endpoint.postRecipeImage(recipeId, imageInputStream);

		verify(repository).updateRecipe(recipeCaptor.capture());
		final Recipe recipeBeingSaved = recipeCaptor.getValue();
		assertThat(recipeBeingSaved).isEqualTo(expectedRecipeBeingSaved);
	}

	private Recipe buildRecipeWithImageUrl(String imageUrl) {
		return new Recipe(new Recipe.RecipeId("hi"), "recipeName", "recipeContent", new Recipe.UserId("itsme"), imageUrl);
	}
}