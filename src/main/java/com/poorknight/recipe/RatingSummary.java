package com.poorknight.recipe;

import java.util.Objects;

/**
 * Read-time aggregate of a recipe's per-user ratings: the average (rounded to one
 * decimal) and the number of ratings. A recipe with no ratings is {0.0, 0}.
 */
public class RatingSummary {

	private final double average;
	private final int count;

	public RatingSummary(final double average, final int count) {
		this.average = average;
		this.count = count;
	}

	public static RatingSummary none() {
		return new RatingSummary(0.0, 0);
	}

	public double getAverage() {
		return average;
	}

	public int getCount() {
		return count;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final RatingSummary that = (RatingSummary) o;
		return Double.compare(average, that.average) == 0 && count == that.count;
	}

	@Override
	public int hashCode() {
		return Objects.hash(average, count);
	}

	@Override
	public String toString() {
		return "RatingSummary{average=" + average + ", count=" + count + '}';
	}
}
