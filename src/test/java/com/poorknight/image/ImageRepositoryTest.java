package com.poorknight.image;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UUID.class, ImageRepository.class})
public class ImageRepositoryTest {

	private static final String HTTPS_URL = "https://helloThere";
	private static final String HTTP_ONLY_URL = "http://helloThere";

	private UUID uuid;
	private String imageId;
	private String imageUrl;
	private InputStream imageInputStream;

	@InjectMocks
	private ImageRepository repository;

	@Mock
	private ImageS3Repository s3Repository;

	@Before
	public void setUp() throws Exception {
		uuid = UUID.randomUUID();
		imageId = uuid.toString();
		imageInputStream = new ByteArrayInputStream("image".getBytes());
		imageUrl = RandomStringUtils.random(50);

		PowerMockito.mockStatic(UUID.class);

		when(UUID.randomUUID()).thenReturn(uuid);
		when(s3Repository.saveNewImage(imageInputStream, imageId)).thenReturn(imageUrl);
	}

	@Test
	public void saveImageGeneratesAnIdForTheImageAndDelegatesToS3Repository() throws Exception {
		Image savedImage = repository.saveNewImage(imageInputStream, "owningUser");

		assertThat(savedImage.getImageId()).isEqualTo(imageId);
		assertThat(savedImage.getImageUrl()).isEqualTo(imageUrl);
		assertThat(savedImage.getOwningUserId()).isEqualTo("owningUser");
	}

	@Test
	public void savedImageCanBeRetrieved() throws Exception {
//		repository.saveNewImage(imageInputStream, "owningUser");
//
//		Image retrievedImage = repository.findImage(imageId);
//
//		assertThat(retrievedImage.getImageId()).isEqualTo(imageId);
//		assertThat(retrievedImage.getImageUrl()).isEqualTo(HTTP_ONLY_URL);
//		assertThat(retrievedImage.getOwningUserId()).isEqualTo("owningUser");
	}
}