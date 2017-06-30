package com.poorknight.image;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Image {

	private final String imageId;
	private final String imageUrl;
	private final String owningUserId;

	public Image(final String imageId, final String imageUrl, final String owningUserId) {
		this.imageId = imageId;
		this.imageUrl = imageUrl;
		this.owningUserId = owningUserId;
	}

	public String getImageId() {
		return imageId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getOwningUserId() {
		return owningUserId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Image image = (Image) o;
		return Objects.equal(imageId, image.imageId) && Objects.equal(imageUrl, image.imageUrl) && Objects.equal(
				owningUserId,
				image.owningUserId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(imageId, imageUrl, owningUserId);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("imageId", imageId).add("imageUrl", imageUrl).add("owningUserId",
				owningUserId).toString();
	}
}
