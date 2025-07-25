package com.poorknight.recipe.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

public class RecipeSearchStringParserTest {

	private final RecipeSearchStringParser parser = new RecipeSearchStringParser();

	@Test
	public void parsesNullToEmptyList() throws Exception {
		final List<SearchTag> results = parser.parseSearchString(null);
		assertThat(results).isEmpty();
	}

	@Test
	public void parsesEmptyStringToEmptySet() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("");
		assertThat(results).isEmpty();
	}

	@Test
	public void parsesSearchWithNoSpacesAsASingleTag() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("hi");
		assertThat(results).containsExactlyInAnyOrder(new SearchTag("hi"));
	}

	@Test
	public void callsDaoWithCorrectArgumentsWithSpaces() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("hi there");
		assertThat(results).containsExactlyInAnyOrder(new SearchTag("hi"), new SearchTag("there"));
	}

	@Test
	public void callsDaoWithCorrectArgumentsLeadingSpaces() throws Exception {
		final List<SearchTag> results = parser.parseSearchString(" hi");
		assertThat(results).containsExactlyInAnyOrder(new SearchTag("hi"));
	}

	@Test
	public void callsDaoWithCorrectArgumentsTrailingSpaces() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("hi  ");
		assertThat(results).containsExactlyInAnyOrder(new SearchTag("hi"));
	}

	@Test
	public void callsDaoWithCorrectArgumentsMultipleMiddleSpaces() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("h   i");
		assertThat(results).containsExactlyInAnyOrder(new SearchTag("h"), new SearchTag("i"));
	}

	@Test
	public void callsDaoWithCorrectArguments_WithRepeatedStrings() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("hi hi hi");
		assertThat(results).containsExactlyInAnyOrder(new SearchTag("hi"));
	}

	@Test
	public void callsDaoWithCorrectArguments_WithRepeatedDifferentCaseStrings() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("hi HI Hi hI");
		assertThat(results).containsExactlyInAnyOrder(new SearchTag("hi"));
	}

	@Test
	public void tagsAreOrderedInTheOrderTheyWereFirstFound() throws Exception {
		final List<SearchTag> results = parser.parseSearchString("hi its me and its also me @ what");
		assertThat(results).containsExactly( //
				new SearchTag("hi"), //
				new SearchTag("its"), //
				new SearchTag("me"), //
				new SearchTag("and"), //
				new SearchTag("also"), //
				new SearchTag("@"), //
				new SearchTag("what"));
	}

	@Test
	public void resultsAreNotModifiable() throws Exception {
		try {
			final List<SearchTag> results = parser.parseSearchString("hi");
			results.add(new SearchTag("trying to add"));
			fail("expecting exception");
		} catch (Exception e) {
			assertThat(e).isInstanceOf(UnsupportedOperationException.class);
		}
	}
}
