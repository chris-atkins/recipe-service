package com.poorknight.recipe;

/** Request body for rating a recipe: { value }. */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ApiRatingRequest {
	private int value;

	public ApiRatingRequest() {
	}

	public ApiRatingRequest(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(final int value) {
		this.value = value;
	}
}
