package org.reactome.curation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParseUtilities {

	public static List<String> getListFromCSVString(String csvLine) {
		return Arrays.asList(csvLine.split(","));
	}

	public static List<Long> convertStringListToLongList(List<String> stringList) {
		return stringList
			.stream()
			.map(String::trim)
			.filter(str -> !str.isEmpty())
			.map(Long::parseLong)
			.collect(Collectors.toList());
	}

	public static String convertListToString(List<?> list) {
		return list.stream().map(Object::toString).collect(Collectors.joining(","));
	}

	public static int parseReleaseVersion(String releaseVersion) {
		return convertToInt(
			releaseVersion.startsWith("V") ? releaseVersion.substring(1) : releaseVersion
		);
	}

	public static int convertToInt(String stringValue) {
		return !stringValue.isEmpty() ? Integer.parseInt(stringValue) : -1;
	}

	public static Boolean getBooleanFromYesNo(String yesOrNo) {
		if (yesOrNo.toLowerCase().startsWith("yes")) {
			return true;
		} else if (yesOrNo.toLowerCase().startsWith("no")) {
			return false;
		} else if (yesOrNo.isEmpty()) {
			return null;
		} else {
			throw new IllegalArgumentException("String parameter must be 'yes' or 'no', but received - " + yesOrNo);
		}
	}

	public static boolean equalOrBothNull(Object obj1, Object obj2) {
		if (obj1 == null) {
			return obj2 == null;
		} else {
			return obj1.equals(obj2);
		}
	}
}
