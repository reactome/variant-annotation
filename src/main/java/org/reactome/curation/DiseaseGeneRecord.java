package org.reactome.curation;

import java.io.IOException;
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

	private String protein;
	private String omimIdentifier;
	private String uniprotId;
	private String variantName;
	private String variantId;
	private List<String> disease;
	private String mutation;
	private String gofLofNull;
	private String wtReactomePathway;
	private List<Long> selectedPubMedIds;
	private List<Long> cosmicPubMedIds;
	private String curator;
	private List<String> consequence;
	private List<String> normalReaction;
	private String comments;
	private String status;
	private int releaseVersion;

	private DiseaseGeneRecord(String tsvLine) {

		int currentField = 0;

		this.protein = getField(tsvLine, currentField++);
		this.omimIdentifier = getField(tsvLine, currentField++);
		this.uniprotId = getField(tsvLine, currentField++);
		this.variantName = getField(tsvLine, currentField++);
		this.variantId = getField(tsvLine, currentField++);
		this.disease = getListFromCSVString(getField(tsvLine, currentField++));
		this.mutation = getField(tsvLine, currentField++);
		this.gofLofNull = getField(tsvLine, currentField++);
		this.wtReactomePathway = getField(tsvLine, currentField++);
		this.selectedPubMedIds = convertStringListToLongList(getListFromCSVString(getField(tsvLine, currentField++)));
		this.cosmicPubMedIds = convertStringListToLongList(getListFromCSVString(getField(tsvLine, currentField++)));
		this.curator = getField(tsvLine, currentField++);
		this.consequence = getListFromCSVString(getField(tsvLine, currentField++));
		this.normalReaction = getListFromCSVString(getField(tsvLine, currentField++));
		this.comments = getField(tsvLine, currentField++);
		this.status = getField(tsvLine, currentField++);
		this.releaseVersion = parseReleaseVersion((getField(tsvLine, currentField++)));
	}

	public static List<DiseaseGeneRecord> parseDiseaseGeneRecords(String tsvFilePath) throws IOException {
		return GenericRecord.parseRecords(tsvFilePath, EXPECTED_HEADER, DiseaseGeneRecord::new);
	}

	public String getProtein() {
		return protein;
	}

	public String getOmimIdentifier() {
		return omimIdentifier;
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public String getVariantName() {
		return variantName;
	}

	public String getVariantId() {
		return variantId;
	}

	public List<String> getDisease() {
		return disease;
	}

	public String getDiseaseAsString() {
		return convertListToString(getDisease());
	}

	public String getMutation() {
		return mutation;
	}

	public String getGofLofNull() {
		return gofLofNull;
	}

	public String getWtReactomePathway() {
		return wtReactomePathway;
	}

	public List<Long> getSelectedPubMedIds() {
		return selectedPubMedIds;
	}

	public String getSelectedPubMedIdsAsString() {
		return convertListToString(getSelectedPubMedIds());
	}

	public List<Long> getCosmicPubMedIds() {
		return cosmicPubMedIds;
	}

	public String getCosmicPubMedIdsString() {
		return convertListToString(getCosmicPubMedIds());
	}

	public String getCurator() {
		return curator;
	}

	public List<String> getConsequence() {
		return consequence;
	}

	public String getConsequenceAsString() {
		return convertListToString(getConsequence());
	}

	public List<String> getNormalReaction() {
		return normalReaction;
	}

	public String getNormalReactionAsString() {
		return convertListToString(getNormalReaction());
	}

	public String getComments() {
		return comments;
	}

	public String getStatus() {
		return status;
	}

	public int getReleaseVersion() {
		return releaseVersion;
	}

	public String getReleaseVersionAsString() {
		if (releaseVersion == -1) {
			return "";
		} else {
			return Integer.toString(releaseVersion);
		}
	}
}
