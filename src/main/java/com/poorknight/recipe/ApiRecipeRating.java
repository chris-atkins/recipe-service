package com.poorknight.recipe;

import java.util.Objects;

/** API shape of a recipe's rating aggregate: { average, count }. */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ApiRecipeRating {
	private double average;
	private int count;

	public ApiRecipeRating() {
	}

	public ApiRecipeRating(final double average, final int count) {
		this.average = average;
		this.count = count;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(final double average) {
		this.average = average;
	}

	public int getCount() {
		return count;
	}

	public void setCount(final int count) {
		this.count = count;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final ApiRecipeRating that = (ApiRecipeRating) o;
		return Double.compare(average, that.average) == 0 && count == that.count;
	}

	@Override
	public int hashCode() {
		return Objects.hash(average, count);
	}

	@Override
	public String toString() {
		return "ApiRecipeRating{average=" + average + ", count=" + count + '}';
	}
}
