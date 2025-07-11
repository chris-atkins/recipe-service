package com.poorknight.recipe;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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