package org.reactome.curation;

import java.io.IOException;
import java.util.List;

import static org.reactome.curation.GenericRecord.getField;
import static org.reactome.curation.ParseUtilities.convertToInt;
import static org.reactome.curation.ParseUtilities.getBooleanFromYesNo;

public class HighPriorityVariantRecord {
	private final static String EXPECTED_HEADER = String.join(
		"\t",
		"Variant name",
		"Count",
		"Protein in Reactome",
		"gene has variants in Reactome",
		"specific variant in Reactome"
	);

	private int count;
	private String geneHasVariantsInReactome;
	private String specificVariantsInReactome;
	private CommonAnnotations commonAnnotations;

	private HighPriorityVariantRecord(String tsvLine) {
		int currentField = 0;

		String variantName = getField(tsvLine, currentField++);
		this.count = convertToInt(getField(tsvLine, currentField++));
		Boolean isProteinInReactome = getBooleanFromYesNo(getField(tsvLine, currentField++));
		this.geneHasVariantsInReactome = getField(tsvLine, currentField++);
		this.specificVariantsInReactome = getField(tsvLine, currentField++);

		this.commonAnnotations = new CommonAnnotations.Builder()
			.withRecordLine(tsvLine)
			.withVariantName(variantName)
			.isProteinInReactome(isProteinInReactome)
			.build();
	}

	public static List<HighPriorityVariantRecord> parseHighPriorityVariantRecords(String tsvFilePath) throws IOException {
		return GenericRecord.parseRecords(tsvFilePath, EXPECTED_HEADER, HighPriorityVariantRecord::new);
	}

	@Override
	public String toString() {
		return this.commonAnnotations.getRecordLine();
	}

	public String getVariantName() {
		return this.commonAnnotations.getVariantName();
	}

	public int getCount() {
		return this.count;
	}

	public String getIsProteinInReactomeAsString() {
		return this.commonAnnotations.getIsProteinInReactomeAsString();
	}

	public Boolean proteinIsInReactome() {
		return this.commonAnnotations.proteinIsInReactome();
	}

	public static Boolean getIsProteinInReactome(List<HighPriorityVariantRecord> highPriorityVariantRecords) {
		if (highPriorityVariantRecords.isEmpty() ||
			highPriorityVariantRecords.stream().allMatch(r -> r.proteinIsInReactome() == null)) {
			return null;
		}

		return highPriorityVariantRecords.stream().anyMatch(HighPriorityVariantRecord::proteinIsInReactome);
	}

	public String geneHasVariantsInReactome() {
		return this.geneHasVariantsInReactome;
	}

	public String specificVariantsAreInReactome() {
		return this.specificVariantsInReactome;
	}
}