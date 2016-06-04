package com.poorknight.recipe.search;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RecipeSearchStringParser {

	public List<SearchTag> parseSearchString(final String searchString) {
		if (searchString == null || searchString.isEmpty()) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(new LinkedList<SearchTag>(parse(searchString)));
	}

	private Set<SearchTag> parse(final String searchString) {

		final String[] searchParts = splitSearchStringIntoParts(searchString);

		final Set<SearchTag> results = new LinkedHashSet<>();
		for (final String partial : searchParts) {
			addPartialToResultsIfValid(results, partial);
		}

		return results;
	}

	private String[] splitSearchStringIntoParts(final String searchString) {
		return searchString.split(" ");
	}

	private void addPartialToResultsIfValid(final Set<SearchTag> searchTagResults, final String partial) {
		if (isValid(partial)) {
			searchTagResults.add(new SearchTag(partial));
		}
	}

	private boolean isValid(final String partial) {
		return !partial.isEmpty();
	}
}
