package com.poorknight.recipe.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.poorknight.recipe.search.SearchTag;

@RunWith(JUnit4.class)
public class SearchTagTest {

	@Test
	public void changesValuesToLowerCase() throws Exception {
		final SearchTag searchTag = new SearchTag("HI");
		assertThat(searchTag.getValue()).isEqualTo("hi");
	}
}
