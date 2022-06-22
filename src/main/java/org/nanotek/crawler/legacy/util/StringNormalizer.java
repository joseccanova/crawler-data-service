package org.nanotek.crawler.legacy.util;


import java.text.Normalizer;

public interface StringNormalizer {

	/**
	 * Remove toda a acentuação da string substituindo por caracteres simples sem acento.
	 */
	default String unaccent(String src) {
		return Normalizer
				.normalize(src, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");
	}
	
}