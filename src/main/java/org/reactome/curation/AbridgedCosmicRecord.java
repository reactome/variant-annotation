package org.reactome.curation;

import java.io.IOException;
import java.util.Collections;
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

	private String mutationAA;
	private Boolean highPriority;
	private String mutationDescription;
	private CommonAnnotations commonAnnotations;

	private AbridgedCosmicRecord(String tsvLine) {
		int currentField = 0;

		String protein = getField(tsvLine, currentField++);
		this.mutationAA = getField(tsvLine, currentField++);
		String variantName = getField(tsvLine, currentField++);
		String variantId = getField(tsvLine, currentField++);
		long cosmicPubMedId = Long.parseLong(getField(tsvLine, currentField++));
		Boolean isProteinInReactome = getBooleanFromYesNo(getField(tsvLine, currentField++));
		Boolean areAnyVariantsAnnotated = getBooleanFromYesNo(getField(tsvLine, currentField++));
		this.highPriority = getBooleanFromYesNo(getField(tsvLine, currentField++));
		String status = getField(tsvLine, currentField++);
		int releaseVersion = parseReleaseVersion(getField(tsvLine, currentField++));
		this.mutationDescription = getField(tsvLine, currentField++);

		this.commonAnnotations = new CommonAnnotations.Builder()
			.withRecordLine(tsvLine)
			.withProtein(protein)
			.withVariantName(variantName)
			.withVariantIds(Collections.singletonList(variantId))
			.withCosmicPubMedIds(Collections.singletonList(cosmicPubMedId))
			.isProteinInReactome(isProteinInReactome)
			.areAnyVariantsAnnotated(areAnyVariantsAnnotated)
			.withStatus(status)
			.withReleaseVersion(releaseVersion)
			.build();
	}

	public static List<AbridgedCosmicRecord> parseAbridgedCosmicRecords(String tsvFilePath) throws IOException {
		return GenericRecord.parseRecords(tsvFilePath, EXPECTED_HEADER, AbridgedCosmicRecord::new);
	}

	public boolean isSameOtherThanVariantAndCosmicPubMedId(AbridgedCosmicRecord abridgedCosmicRecord) {
		if (this == abridgedCosmicRecord) {
			return true;
		}

		return
			this.getProtein().equals(abridgedCosmicRecord.getProtein()) &&
			this.getMutationAA().equals(abridgedCosmicRecord.getMutationAA()) &&
			this.getVariantName().equals(abridgedCosmicRecord.getVariantName()) &&
			equalOrBothNull(this.proteinIsInReactome(), abridgedCosmicRecord.proteinIsInReactome()) &&
			equalOrBothNull(this.anyVariantsAreAnnotated(), abridgedCosmicRecord.anyVariantsAreAnnotated()) &&
			equalOrBothNull(this.isHighPriority(), abridgedCosmicRecord.isHighPriority()) &&
			this.getStatus().equals(abridgedCosmicRecord.getStatus()) &&
			this.getReleaseVersion() == abridgedCosmicRecord.getReleaseVersion() &&
			this.getMutationDescription().equals(abridgedCosmicRecord.getMutationDescription());
	}

	@Override
	public String toString() {
		return this.commonAnnotations.getRecordLine();
	}

	public String getProtein() {
		return this.commonAnnotations.getProtein();
	}

	public String getMutationAA() {
		return this.mutationAA;
	}

	public String getVariantName() {
		return this.commonAnnotations.getVariantName();
	}

	public String getVariantId() {
		return this.commonAnnotations.getVariantIds().get(0);
	}

	public long getCosmicPubMedId() {
		return this.commonAnnotations.getCosmicPubMedIds().get(0);
	}

	public Boolean proteinIsInReactome() {
		return this.commonAnnotations.proteinIsInReactome();
	}

	public String getIsProteinInReactomeAsString() {
		return this.commonAnnotations.getIsProteinInReactomeAsString();
	}

	public Boolean anyVariantsAreAnnotated() {
		return this.commonAnnotations.anyVariantsAreAnnotated();
	}

	public Boolean isHighPriority() {
		return this.highPriority;
	}

	public String getStatus() {
		return this.commonAnnotations.getStatus();
	}

	public String getReleaseVersionAsString() {
		return this.commonAnnotations.getReleaseVersionAsString();
	}

	public int getReleaseVersion() {
		return this.commonAnnotations.getReleaseVersion();
	}

	public String getMutationDescription() {
		return this.mutationDescription;
	}
}
