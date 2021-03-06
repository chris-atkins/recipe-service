package com.poorknight.image;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class ApiImage {

	private String imageId;
	private String imageUrl;

	public ApiImage(final String imageId, final String imageUrl) {
		this.imageId = imageId;
		this.imageUrl = imageUrl;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(final String imageId) {
		this.imageId = imageId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(final String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final ApiImage apiImage = (ApiImage) o;
		return Objects.equal(imageId, apiImage.imageId) && Objects.equal(imageUrl, apiImage.imageUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(imageId, imageUrl);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("imageId", imageId).add("imageUrl", imageUrl).toString();
	}
}
