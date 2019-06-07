import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HighPriorityVariantRecord {
	private final static String EXPECTED_HEADER = String.join("\t",
		"Variant name", "Count", "Protein in Reactome", "gene has variants in Reactome", "specific variant in Reactome"
	);

	private List<String> fields;

	private String variantName;
	private int count;
	private boolean isProteinInReactome;
	private String geneHasVariantsInReactome;
	private String specificVariantsInReactome;

	private HighPriorityVariantRecord(String tsvLine) {
		fields = Arrays.asList(tsvLine.split("\t"));

		int currentField = 0;
		this.variantName = getField(currentField++);
		this.count = Integer.parseInt(getField(currentField++));
		this.isProteinInReactome = getBooleanFromYesNo(getField(currentField++));
		this.geneHasVariantsInReactome = getField(currentField++);
		this.specificVariantsInReactome = getField(currentField++);
	}

	public static List<HighPriorityVariantRecord> parseHighPriorityVariantRecords(String tsvFilePath) throws IOException {
		List<String> tsvFileLines = Files.readAllLines(Paths.get(tsvFilePath));

		String header = tsvFileLines.remove(0);
		if (!isExpectedHeader(header)) {
			throw new IllegalArgumentException(
				tsvFilePath + " does not have the expected header.\n" +
				"\tExpected: " + EXPECTED_HEADER + "\n" +
				"\tReceived: " + header + "\n");
		}

		return tsvFileLines
			.stream()
			.map(HighPriorityVariantRecord::new)
			.collect(Collectors.toList());
	}

	private static boolean isExpectedHeader(String parsedHeader) {
		return parsedHeader.equals(EXPECTED_HEADER);
	}

	public String getVariantName() {
		return variantName;
	}

	public int getCount() {
		return count;
	}

	public boolean proteinIsInReactome() {
		return isProteinInReactome;
	}

	public String geneHasVariantsInReactome() {
		return geneHasVariantsInReactome;
	}

	public String specificVariantsAreInReactome() {
		return specificVariantsInReactome;
	}

	private String getField(int fieldIndex) {
		if (fieldIndex >= fields.size()) {
			return "";
		}

		return fields.get(fieldIndex);
	}

	private boolean getBooleanFromYesNo(String yesOrNo) {
		if (yesOrNo.toLowerCase().startsWith("yes")) {
			return true;
		} else if (yesOrNo.toLowerCase().startsWith("no") || yesOrNo.isEmpty()) {
			return false;
		} else {
			throw new IllegalArgumentException("String parameter must be 'yes' or 'no', but received - " + yesOrNo);
		}
	}
}