package com.poorknight.api;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.poorknight.recipe.RecipeImage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

	@Test
	public void endpointGeneratesAnIdForTheImageAndStoresItToAWSWWhileReplacingURLWithHttp() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final String bucketName = "myrecipeconnection.com.images";
		final String key = uuid.toString();
		final AmazonS3 s3 = Mockito.mock(AmazonS3.class);
		final InputStream imageInputStream = new ByteArrayInputStream("image".getBytes());

		PowerMockito.mockStatic(AmazonS3ClientBuilder.class);
		PowerMockito.mockStatic(UUID.class);

		ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
		when(UUID.randomUUID()).thenReturn(uuid);
		when(AmazonS3ClientBuilder.defaultClient()).thenReturn(s3);
		when(s3.getUrl(bucketName, key)).thenReturn(new URL(HTTPS_URL));

		RecipeImageEndpoint endpoint = new RecipeImageEndpoint();
		Response postImageResponse = endpoint.postRecipeImage(imageInputStream);

		verify(s3).putObject(captor.capture());
		final PutObjectRequest s3RequestObject = captor.getValue();
		assertThat(s3RequestObject.getBucketName()).isEqualTo(bucketName);
		assertThat(s3RequestObject.getKey()).isEqualTo(key);
		assertThat(s3RequestObject.getInputStream()).isEqualTo(imageInputStream);
		assertThat(s3RequestObject.getCannedAcl()).isEqualTo(CannedAccessControlList.PublicRead);

		final RecipeImage imageResponse = (RecipeImage) postImageResponse.getEntity();
		assertThat(imageResponse.getImageId()).isEqualTo(key);
		assertThat(imageResponse.getImageUrl()).isEqualTo(HTTP_ONLY_URL);
	}
}