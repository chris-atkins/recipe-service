package com.poorknight.recipe.save;

public class TextToHtmlTranformer {

	private static final String HTML_NEWLINE = "<br/>";
	private static final String[] NEWLINE_REGEXS = { "\\r\\n", "\\n" }; // order matters

	public String translate(final String originalText) {
		String results = originalText;
		for (final String regex : NEWLINE_REGEXS) {
			results = results.replaceAll(regex, HTML_NEWLINE);
		}
		return results;
	}
}
