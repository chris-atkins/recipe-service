package com.poorknight.image;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UUID.class, ImageRepository.class})
public class ImageRepositoryTest {

	@InjectMocks
	private ImageRepository repository;

	@Mock
	private ImageS3Repository s3Repository;

	@Mock
	private ImageDBRepository dbRepository;

	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(UUID.class);

	}

	@Test
	public void saveImageGeneratesAnIdForTheImageAndDelegatesToS3AndDBRepositories() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final String imageId = uuid.toString();
		final InputStream imageInputStream = new ByteArrayInputStream("image".getBytes());
		final String imageUrl = random(50);
		final String userId = random(20);
		final Image expectedImage = new Image(random(5), random(10), random(15));

		when(UUID.randomUUID()).thenReturn(uuid);
		when(s3Repository.saveNewImage(imageInputStream, imageId)).thenReturn(imageUrl);
		when(dbRepository.saveNewImage(imageId, imageUrl, userId)).thenReturn(expectedImage);

		final Image savedImage = repository.saveNewImage(imageInputStream, userId);

		assertThat(savedImage).isEqualTo(expectedImage);
	}

	@Test
	public void findImageDelegatesToDBRepository() throws Exception {
		final Image expectedImage = new Image(random(5), random(10), random(15));
		final String imageId = random(20);
		when(dbRepository.findImage(imageId)).thenReturn(expectedImage);

		final Image foundImage = repository.findImage(imageId);

		assertThat(foundImage).isEqualTo(expectedImage);
	}

	@Test
	public void deleteImageDelegatesToS3AndDBRepositories() throws Exception {
		final String imageId = random(5);
		final String imageUrl = random(10);
		final String userId = random(15);
		final Image expectedImage = new Image(imageId, imageUrl, userId);

		when(dbRepository.findImage(imageId)).thenReturn(expectedImage);

		repository.deleteImage(imageId, userId);

		Mockito.verify(dbRepository).deleteImage(imageId);
		Mockito.verify(s3Repository).deleteImage(imageId);
	}

	@Test
	public void deleteWillThrowExceptionIfUserDoesNotOwnImage() throws Exception {
		try {
			final String imageId = random(5);
			final Image expectedImage = new Image(imageId, random(10), "owning user");
			when(dbRepository.findImage(imageId)).thenReturn(expectedImage);

			repository.deleteImage(imageId, "other user");

			fail("expecting exception");
		} catch (ImageDeleteNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("Requesting user does not own the image, so it cannot be deleted.");
			assertThat(e.getResponse().getStatus()).isEqualTo(403);
		}
	}

	@Test
	public void deleteWillThrowExceptionIfUserIsNull() throws Exception {
		try {
			repository.deleteImage("id", null);
			fail("expecting exception");
		} catch (ImageDeleteNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("No requesting user has been specified when requesting an image be deleted, so it cannot be deleted.");
			assertThat(e.getResponse().getStatus()).isEqualTo(403);
		}
	}

	@Test
	public void deleteWillThrowExceptionIfUserIsEmpty() throws Exception {
		try {
			repository.deleteImage("id", "");
			fail("expecting exception");
		} catch (ImageDeleteNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("No requesting user has been specified when requesting an image be deleted, so it cannot be deleted.");
			assertThat(e.getResponse().getStatus()).isEqualTo(403);
		}
	}

}