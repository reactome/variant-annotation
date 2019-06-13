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

	private String variantName;
	private int count;
	private Boolean isProteinInReactome;
	private String geneHasVariantsInReactome;
	private String specificVariantsInReactome;

	private HighPriorityVariantRecord(String tsvLine) {

		int currentField = 0;

		this.variantName = getField(tsvLine, currentField++);
		this.count = convertToInt(getField(tsvLine, currentField++));
		this.isProteinInReactome = getBooleanFromYesNo(getField(tsvLine, currentField++));
		this.geneHasVariantsInReactome = getField(tsvLine, currentField++);
		this.specificVariantsInReactome = getField(tsvLine, currentField++);
	}

	public static List<HighPriorityVariantRecord> parseHighPriorityVariantRecords(String tsvFilePath) throws IOException {
		return GenericRecord.parseRecords(tsvFilePath, EXPECTED_HEADER, HighPriorityVariantRecord::new);
	}

	public String getVariantName() {
		return variantName;
	}

	public int getCount() {
		return count;
	}

	public Boolean proteinIsInReactome() {
		return isProteinInReactome;
	}

	public String geneHasVariantsInReactome() {
		return geneHasVariantsInReactome;
	}

	public String specificVariantsAreInReactome() {
		return specificVariantsInReactome;
	}
}