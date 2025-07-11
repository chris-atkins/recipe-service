package com.poorknight.api;

import com.poorknight.image.ApiImage;
import com.poorknight.image.Image;
import com.poorknight.image.ImageRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageEndpointTest {

	@InjectMocks
	private ImageEndpoint endpoint;

	@Mock
	private ImageRepository repository;

	@Mock
	private InputStream imageInputStream;

	@Test
	public void postImageDelegatesToRepository() throws Exception {
		final String requestingUserId = "requestingUserId";
		final String imageId = "imageId";
		final String imageUrl = "imageUrl";
		final ApiImage expectedRespose = new ApiImage(imageId, imageUrl);
		when(repository.saveNewImage(imageInputStream, requestingUserId)).thenReturn(new Image(imageId, imageUrl, requestingUserId));

		final ApiImage apiImage = endpoint.postImage(imageInputStream, requestingUserId);
		assertThat(apiImage).isEqualTo(expectedRespose);
	}

	@Test
	public void deleteDelegatesToRepository() throws Exception {
		final String imageId = RandomStringUtils.random(20);
		final String requestingUserIdString = RandomStringUtils.random(10);
		endpoint.deleteImage(imageId, requestingUserIdString);

		verify(repository).deleteImage(imageId, requestingUserIdString);
	}
}