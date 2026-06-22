package com.poorknight.recipe;

/** Request body for adding a tag to a recipe: { tag }. */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ApiTagRequest {
	private String tag;

	public ApiTagRequest() {
	}

	public ApiTagRequest(final String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(final String tag) {
		this.tag = tag;
	}
}
