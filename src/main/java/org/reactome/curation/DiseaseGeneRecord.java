package org.reactome.curation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.reactome.curation.GenericRecord.getField;
import static org.reactome.curation.ParseUtilities.*;

public class DiseaseGeneRecord {
	private final static String EXPECTED_HEADER = String.join(
		"\t",
		"Protein",
		"OMIM identifier",
		"UniProt ID",
		"Variant name",
		"Variant ID",
		"Disease",
		"Mutation",
		"GOF/LOF/null",
		"WT Reactome Pathway",
		"Selected_Pubmed_PMID",
		"COSMIC_Pubmed_PMID",
		"Curator",
		"Consequence",
		"NormalReaction",
		"Comments",
		"Status",
		"ReleaseVersion"
	);

	private String omimIdentifier;
	private String uniprotId;
	private List<String> disease;
	private String mutation;
	private String gofLofNull;
	private String wtReactomePathway;
	private List<Long> selectedPubMedIds;
	private String curator;
	private List<String> consequence;
	private List<String> normalReaction;
	private String comments;
	private CommonAnnotations commonAnnotations;

	private DiseaseGeneRecord(String tsvLine) {
		int currentField = 0;

		String protein = getField(tsvLine, currentField++);
		this.omimIdentifier = getField(tsvLine, currentField++);
		this.uniprotId = getField(tsvLine, currentField++);
		String variantName = getField(tsvLine, currentField++);
		String variantId = getField(tsvLine, currentField++);
		this.disease = getListFromCSVString(getField(tsvLine, currentField++));
		this.mutation = getField(tsvLine, currentField++);
		this.gofLofNull = getField(tsvLine, currentField++);
		this.wtReactomePathway = getField(tsvLine, currentField++);
		this.selectedPubMedIds = convertStringListToLongList(getListFromCSVString(getField(tsvLine, currentField++)));
		List<Long> cosmicPubMedIds = convertStringListToLongList(getListFromCSVString(getField(tsvLine, currentField++)));
		this.curator = getField(tsvLine, currentField++);
		this.consequence = getListFromCSVString(getField(tsvLine, currentField++));
		this.normalReaction = getListFromCSVString(getField(tsvLine, currentField++));
		this.comments = getField(tsvLine, currentField++);
		String status = getField(tsvLine, currentField++);
		int releaseVersion = parseReleaseVersion((getField(tsvLine, currentField++)));

		this.commonAnnotations = new CommonAnnotations.Builder()
			.withRecordLine(tsvLine)
			.withProtein(protein)
			.withVariantName(variantName)
			.withVariantIds(Collections.singletonList(variantId))
			.withCosmicPubMedIds(cosmicPubMedIds)
			.withStatus(status)
			.withReleaseVersion(releaseVersion)
			.build();
	}

	public static List<DiseaseGeneRecord> parseDiseaseGeneRecords(String tsvFilePath) throws IOException {
		return GenericRecord.parseRecords(tsvFilePath, EXPECTED_HEADER, DiseaseGeneRecord::new);
	}

	@Override
	public String toString() {
		return this.commonAnnotations.getRecordLine();
	}

	public String getProtein() {
		return this.commonAnnotations.getProtein();
	}

	public String getOmimIdentifier() {
		return this.omimIdentifier;
	}

	public String getUniprotId() {
		return this.uniprotId;
	}

	public String getVariantName() {
		return this.commonAnnotations.getVariantName();
	}

	public String getVariantId() {
		return this.commonAnnotations.getVariantIds().get(0);
	}

	public List<String> getDisease() {
		return this.disease;
	}

	public String getDiseaseAsString() {
		return convertListToString(getDisease());
	}

	public String getMutation() {
		return this.mutation;
	}

	public String getGofLofNull() {
		return this.gofLofNull;
	}

	public String getWtReactomePathway() {
		return this.wtReactomePathway;
	}

	public List<Long> getSelectedPubMedIds() {
		return this.selectedPubMedIds;
	}

	public String getSelectedPubMedIdsAsString() {
		return convertListToString(getSelectedPubMedIds());
	}

	public List<Long> getCosmicPubMedIds() {
		return this.commonAnnotations.getCosmicPubMedIds();
	}

	public String getCosmicPubMedIdsString() {
		return convertListToString(getCosmicPubMedIds());
	}

	public String getCurator() {
		return this.curator;
	}

	public List<String> getConsequence() {
		return this.consequence;
	}

	public String getConsequenceAsString() {
		return convertListToString(getConsequence());
	}

	public List<String> getNormalReaction() {
		return this.normalReaction;
	}

	public String getNormalReactionAsString() {
		return convertListToString(getNormalReaction());
	}

	public String getComments() {
		return this.comments;
	}

	public String getStatus() {
		return this.commonAnnotations.getStatus();
	}

	public int getReleaseVersion() {
		return this.commonAnnotations.getReleaseVersion();
	}

	public String getReleaseVersionAsString() {
		return this.commonAnnotations.getReleaseVersionAsString();
	}
}
