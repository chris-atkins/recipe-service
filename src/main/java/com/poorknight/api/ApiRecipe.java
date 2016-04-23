package com.poorknight.api;

public class ApiRecipe {
	private String id;
	private String name;
	private String content;

	public ApiRecipe() {
		// empty for serialization
	}

	public ApiRecipe(final String id, final String name, final String content) {
		super();
		this.id = id;
		this.name = name;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}
}
