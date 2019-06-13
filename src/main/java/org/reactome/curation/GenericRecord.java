package org.reactome.curation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenericRecord {

	public static <E> List<E> parseRecords(
		String tsvFilePath, String expectedHeader, Function<String, ? extends E> recordMapper
	) throws IOException {

		List<String> tsvFileLines = Files.readAllLines(Paths.get(tsvFilePath));

		String header = tsvFileLines.remove(0);
		if (!header.equals(expectedHeader)) {
			throw new IllegalArgumentException(
				tsvFilePath + " does not have the expected header.\n" +
				"\tExpected: " + expectedHeader + "\n" +
				"\tReceived: " + header + "\n");
		}

		return tsvFileLines
			.stream()
			.map(recordMapper)
			.collect(Collectors.toList());
	}

	public static String getField(String tsvLine, int fieldIndex) {
		List<String> fields = Arrays.asList(tsvLine.split("\t"));

		if (fieldIndex >= fields.size()) {
			return "";
		}

		return fields.get(fieldIndex);
	}
}
