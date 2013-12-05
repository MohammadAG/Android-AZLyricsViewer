package com.mohammadag.azlyricsviewer;

import android.text.Editable;

public class Utils {
	public static String removeNonAlphabeticalChars(String string) {
		return string.replaceAll("\\s","")
				.replaceAll(" ","")
				.replaceAll("[^a-zA-Z0-9\\s]", "");
	}
	
	public static String removeNonAlphabeticalChars(Editable text) {
		return removeNonAlphabeticalChars(text.toString());
	}
}
