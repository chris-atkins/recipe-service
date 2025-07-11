package com.poorknight.recipe.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SearchTagTest {

	@Test
	public void changesValuesToLowerCase() throws Exception {
		final SearchTag searchTag = new SearchTag("HI");
		assertThat(searchTag.getValue()).isEqualTo("hi");
	}
}
