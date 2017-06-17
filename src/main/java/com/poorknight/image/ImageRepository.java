package com.poorknight.image;

import java.io.InputStream;
import java.util.UUID;

public class ImageRepository {

	private ImageS3Repository s3Repository;

	public ImageRepository(final ImageS3Repository s3Repository) {
		this.s3Repository = s3Repository;
	}

	public Image saveNewImage(final InputStream imageInputStream, final String owningUser) {
		final String imageId = UUID.randomUUID().toString();
		final String imageUrl = s3Repository.saveNewImage(imageInputStream, imageId);
		return new Image(imageId, imageUrl, owningUser);
	}

	public Image findImage(final String imageId) {
		return null;
	}
}
