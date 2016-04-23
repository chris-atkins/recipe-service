package com.poorknight.domain;

import com.poorknight.domain.identities.RecipeId;

public class Recipe {

	private final RecipeId id;
	private final String name;
	private final String content;

	public Recipe(final String name, final String content) {
		this(null, name, content);
	}

	public Recipe(final RecipeId id, final String name, final String content) {
		this.id = id;
		this.name = name;
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	public RecipeId getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return "Recipe [id=" + id + ", name=" + name + ", content=" + content + "]";
	}
}
