package com.poorknight.recipe;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RecipeImageTest {

    @Test
    public void toJsonWorksWithNormalValues() {
        var image = new RecipeImage("imageId", "imageUrl");
        Assertions.assertThat(image.toJsonString()).isEqualTo("{\"imageId\":\"imageId\",\"imageUrl\":\"imageUrl\"}");
    }

    @Test
    public void toJsonWorksWithEmptySpace() {
        var image = new RecipeImage("", " ");
        Assertions.assertThat(image.toJsonString()).isEqualTo("{\"imageId\":\"\",\"imageUrl\":\" \"}");
    }

    @Test
    public void toJsonDoesNotIncludeQuotesForNullValues() {
        var image = new RecipeImage(null, null);
        Assertions.assertThat(image.toJsonString()).isEqualTo("{\"imageId\":null,\"imageUrl\":null}");
    }
}