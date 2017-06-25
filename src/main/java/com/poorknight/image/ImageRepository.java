package com.poorknight.image;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.UUID;

public class ImageRepository {

	private ImageS3Repository s3Repository;
	private ImageDBRepository dbRepository;

	public ImageRepository(final ImageS3Repository s3Repository, final ImageDBRepository dbRepository) {
		this.s3Repository = s3Repository;
		this.dbRepository = dbRepository;
	}

	public Image saveNewImage(final InputStream imageInputStream, final String owningUser) {
		final String imageId = UUID.randomUUID().toString();
		final String imageUrl = s3Repository.saveNewImage(imageInputStream, imageId);
		return dbRepository.saveNewImage(new Image(imageId, imageUrl, owningUser));
	}

	public Image findImage(final String imageId) {
		return dbRepository.findImage(imageId);
	}

	public void deleteImage(final String imageId, final String requestingUserId) {
		final Image image = dbRepository.findImage(imageId);

		validateUserPrivilegesForDelete(requestingUserId, image);

		dbRepository.deleteImage(imageId);
		s3Repository.deleteImage(imageId);
	}

	private void validateUserPrivilegesForDelete(final String requestingUserId, final Image image) {
		if(StringUtils.isEmpty(requestingUserId)) {
			throw new ImageDeleteNotAllowedException("No requesting user has been specified when requesting an image be deleted, so it cannot be deleted.");
		}

		if (!image.getOwningUserId().equals(requestingUserId)) {
			throw new ImageDeleteNotAllowedException("Requesting user does not own the image, so it cannot be deleted.");
		}
	}
}
