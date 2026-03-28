package com.poorknight.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@ExtendWith(MockitoExtension.class)
public class ImageRepositoryTest {

	@InjectMocks
	private ImageRepository repository;

	@Mock
	private ImageS3Repository s3Repository;

	@Mock
	private ImageDBRepository dbRepository;

	@Captor
	private ArgumentCaptor<String> imageIdCaptor;

	@Captor
	private ArgumentCaptor<Image> imageCaptor;

	@Test
	public void saveImageGeneratesAnIdForTheImageAndDelegatesToS3AndDBRepositories() throws Exception {
		final InputStream imageInputStream = new ByteArrayInputStream("image".getBytes(Charset.defaultCharset()));
		final String imageUrl = random(50);
		final String userId = random(20);
		final Image expectedImage = new Image(random(5), random(10), random(15));

		Mockito.when(s3Repository.saveNewImage(Mockito.eq(imageInputStream), imageIdCaptor.capture())).thenReturn(imageUrl);
		Mockito.when(dbRepository.saveNewImage(imageCaptor.capture())).thenReturn(expectedImage);

		final Image savedImage = repository.saveNewImage(imageInputStream, userId);

		assertThat(savedImage).isEqualTo(expectedImage);

		final String generatedId = imageIdCaptor.getValue();
		assertThat(generatedId).matches("[a-zA-Z0-9]{24}");

		final Image capturedImage = imageCaptor.getValue();
		assertThat(capturedImage.getImageId()).isEqualTo(generatedId);
		assertThat(capturedImage.getImageUrl()).isEqualTo(imageUrl);
		assertThat(capturedImage.getOwningUserId()).isEqualTo(userId);
	}

	@Test
	public void findImageDelegatesToDBRepository() throws Exception {
		final Image expectedImage = new Image(random(5), random(10), random(15));
		final String imageId = random(20);
		Mockito.when(dbRepository.findImage(imageId)).thenReturn(expectedImage);

		final Image foundImage = repository.findImage(imageId);

		assertThat(foundImage).isEqualTo(expectedImage);
	}

	@Test
	public void deleteImageDelegatesToS3AndDBRepositories() throws Exception {
		final String imageId = random(5);
		final String imageUrl = random(10);
		final String userId = random(15);
		final Image expectedImage = new Image(imageId, imageUrl, userId);

		Mockito.when(dbRepository.findImage(imageId)).thenReturn(expectedImage);

		repository.deleteImage(imageId, userId);

		Mockito.verify(dbRepository).deleteImage(imageId);
		Mockito.verify(s3Repository).deleteImage(imageId);
	}

	@Test
	public void deleteWillThrowExceptionIfUserDoesNotOwnImage() throws Exception {
		try {
			final String imageId = random(5);
			final Image expectedImage = new Image(imageId, random(10), "owning user");
			Mockito.when(dbRepository.findImage(imageId)).thenReturn(expectedImage);

			repository.deleteImage(imageId, "other user");

			fail("expecting exception");
		} catch (final ImageOperationNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("Requesting user does not own the image, so it cannot be deleted.");
			assertThat(e.getResponse().getStatus()).isEqualTo(403);
		}
	}

	@Test
	public void deleteWillThrowExceptionIfUserIsNull() throws Exception {
		try {
			repository.deleteImage("id", null);
			fail("expecting exception");
		} catch (final ImageOperationNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("No requesting user has been specified when requesting an image be deleted, so it cannot be deleted.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void deleteWillThrowExceptionIfUserIsEmpty() throws Exception {
		try {
			repository.deleteImage("id", "");
			fail("expecting exception");
		} catch (final ImageOperationNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("No requesting user has been specified when requesting an image be deleted, so it cannot be deleted.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void saveWillThrowExceptionIfUserIsNull() throws Exception {
		try {
			final InputStream imageInputStream = new ByteArrayInputStream("image".getBytes(Charset.defaultCharset()));
			repository.saveNewImage(imageInputStream, null);
			fail("expecting exception");
		} catch (final ImageOperationNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("No requesting user has been specified when requesting an image be saved, so it cannot be saved.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void saveWillThrowExceptionIfUserIsEmpty() throws Exception {
		try {
			final InputStream imageInputStream = new ByteArrayInputStream("image".getBytes(Charset.defaultCharset()));
			repository.saveNewImage(imageInputStream, "");
			fail("expecting exception");
		} catch (final ImageOperationNotAllowedException e) {
			assertThat(e.getMessage()).isEqualTo("No requesting user has been specified when requesting an image be saved, so it cannot be saved.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}
}