package com.poorknight.image;

import java.io.InputStream;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ImageRepository {

	private ImageS3Repository s3Repository;
	private ImageDBRepository dbRepository;

	public ImageRepository(final ImageS3Repository s3Repository, final ImageDBRepository dbRepository) {
		this.s3Repository = s3Repository;
		this.dbRepository = dbRepository;
	}

	public Image saveNewImage(final InputStream imageInputStream, final String owningUser) {
		validateUserForSave(owningUser);
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

	private void validateUserForSave(final String owningUser) {
		if(isEmpty(owningUser)) {
			throw new ImageOperationNotAllowedException("No requesting user has been specified when requesting an image be saved, so it cannot be saved.",
					ImageOperationNotAllowedException.Reason.NO_USER);
		}
	}

	private void validateUserPrivilegesForDelete(final String requestingUserId, final Image image) {
		if(isEmpty(requestingUserId)) {
			throw new ImageOperationNotAllowedException("No requesting user has been specified when requesting an image be deleted, so it cannot be deleted.",
					ImageOperationNotAllowedException.Reason.NO_USER);
		}

		if (!image.getOwningUserId().equals(requestingUserId)) {
			throw new ImageOperationNotAllowedException("Requesting user does not own the image, so it cannot be deleted.",
					ImageOperationNotAllowedException.Reason.NOT_IMAGE_OWNER);
		}
	}
}
