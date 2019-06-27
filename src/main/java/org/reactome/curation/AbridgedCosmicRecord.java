package org.reactome.curation;

import java.io.IOException;
import java.util.List;

import static org.reactome.curation.GenericRecord.getField;
import static org.reactome.curation.ParseUtilities.*;

public class AbridgedCosmicRecord {
	private final static String EXPECTED_HEADER = String.join(
		"\t",
		"Protein",
		"Mutation AA",
		"Variant name",
		"Variant ID",
		"COSMIC_Pubmed_PMID",
		"Protein_in_Reactome",
		"Any_Variants_Annotated",
		"high priority",
		"Status",
		"ReleaseVersion",
		"Mutation Description"
	);

	private String tsvLine;

	private String protein;
	private String mutationAA;
	private String variantName;
	private String variantId;
	private int cosmicPubMedId;
	private Boolean isProteinInReactome;
	private Boolean areAnyVariantsAnnotated;
	private Boolean highPriority;
	private String status;
	private int releaseVersion;
	private String mutationDescription;

	private AbridgedCosmicRecord(String tsvLine) {
		this.tsvLine = tsvLine;

		int currentField = 0;

		this.protein = getField(tsvLine, currentField++);
		this.mutationAA = getField(tsvLine, currentField++);
		this.variantName = getField(tsvLine, currentField++);
		this.variantId = getField(tsvLine, currentField++);
		this.cosmicPubMedId = Integer.parseInt(getField(tsvLine, currentField++));
		this.isProteinInReactome = getBooleanFromYesNo(getField(tsvLine, currentField++));
		this.areAnyVariantsAnnotated = getBooleanFromYesNo(getField(tsvLine, currentField++));
		this.highPriority = getBooleanFromYesNo(getField(tsvLine, currentField++));
		this.status = getField(tsvLine, currentField++);
		this.releaseVersion = parseReleaseVersion(getField(tsvLine, currentField++));
		this.mutationDescription = getField(tsvLine, currentField++);
	}

	public static List<AbridgedCosmicRecord> parseAbridgedCosmicRecords(String tsvFilePath) throws IOException {
		return GenericRecord.parseRecords(tsvFilePath, EXPECTED_HEADER, AbridgedCosmicRecord::new);
	}

	public boolean isSameOtherThanVariantAndCosmicPubMedId(AbridgedCosmicRecord abridgedCosmicRecord) {
		if (this == abridgedCosmicRecord) {
			return true;
		}

		return
			this.protein.equals(abridgedCosmicRecord.getProtein()) &&
			this.mutationAA.equals(abridgedCosmicRecord.getMutationAA()) &&
			this.variantName.equals(abridgedCosmicRecord.getVariantName()) &&
			equalOrBothNull(this.isProteinInReactome, abridgedCosmicRecord.proteinIsInReactome()) &&
			equalOrBothNull(this.areAnyVariantsAnnotated, abridgedCosmicRecord.anyVariantsAreAnnotated()) &&
			equalOrBothNull(this.highPriority, abridgedCosmicRecord.isHighPriority()) &&
			this.status.equals(abridgedCosmicRecord.getStatus()) &&
			this.releaseVersion == abridgedCosmicRecord.getReleaseVersion() &&
			this.mutationDescription.equals(abridgedCosmicRecord.getMutationDescription());
	}

	@Override
	public String toString() {
		return this.tsvLine;
	}

	public String getProtein() {
		return protein;
	}

	public String getMutationAA() {
		return mutationAA;
	}

	public String getVariantName() {
		return variantName;
	}

	public String getVariantId() {
		return variantId;
	}

	public int getCosmicPubMedId() {
		return cosmicPubMedId;
	}

	public Boolean proteinIsInReactome() {
		return isProteinInReactome;
	}

	public String getIsProteinInReactomeAsString() {
		if (isProteinInReactome == null) {
			return "";
		} else {
			return isProteinInReactome ? "yes" : "no";
		}
	}

	public Boolean anyVariantsAreAnnotated() {
		return areAnyVariantsAnnotated;
	}

	public Boolean isHighPriority() {
		return highPriority;
	}

	public String getStatus() {
		return status;
	}

	public String getReleaseVersionAsString() {
		if (releaseVersion == -1) {
			return "";
		} else {
			return Integer.toString(releaseVersion);
		}
	}

	public int getReleaseVersion() {
		return releaseVersion;
	}

	public String getMutationDescription() {
		return mutationDescription;
	}
}
