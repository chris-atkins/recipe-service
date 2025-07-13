package com.poorknight.recipe;

import java.util.Objects;

public class RecipeImage {

	private String imageId;
	private String imageUrl;

	public RecipeImage() {
		// empty on purpose
	}

	public RecipeImage(final String imageId, final String imageUrl) {
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
		final RecipeImage that = (RecipeImage) o;
		return Objects.equals(imageId, that.imageId) && Objects.equals(imageUrl, that.imageUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(imageId, imageUrl);
	}

	@Override
	public String toString() {
		return "RecipeImage{" +
				"imageId='" + imageId + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				'}';
	}

	public String toJsonString() {
		String imageIdContentString = nullableContentString(imageId);
		String imageUrlContentString = nullableContentString(imageUrl);
		return "{\"imageId\":" + imageIdContentString + ",\"imageUrl\":" + imageUrlContentString + "}";
	}

	private String nullableContentString(String content) {
		return content == null ? "null" : "\"" + content + "\"";
	}
}
