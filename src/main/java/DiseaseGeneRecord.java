import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiseaseGeneRecord {
	private String protein;
	private String omimIdentifier;
	private String uniprotId;
	private String variantName;
	private List<String> variantId;
	private String disease;
	private String mutation;
	private String gofLofNull;
	private String wtReactomePathway;
	private List<Long> selectedPubMedIds;
	private List<Long> cosmicPubMedIds;
	private String curator;
	private String consequence;
	private String normalReaction;
	private String comments;
	private String status;
	private int releaseVersion;

	private DiseaseGeneRecord(String tsvLine) {
		String[] fields = tsvLine.split("\t");

		this.protein = fields[0];
		this.omimIdentifier = fields[1];
		this.uniprotId = fields[2];
		this.variantName = fields[3];
		this.variantId = getListFromCSVString(fields[4]);
		this.disease = fields[4];
		this.mutation = fields[5];
		this.gofLofNull = fields[6];
		this.wtReactomePathway = fields[7];
		this.selectedPubMedIds = convertStringListToLongList(getListFromCSVString(fields[8]));
		this.cosmicPubMedIds = convertStringListToLongList(getListFromCSVString(fields[9]));
		this.curator = fields[10];
		this.consequence = fields[11];
		this.normalReaction = fields[12];
		this.comments = fields[13];
		this.status = fields[14];
		this.releaseVersion = Integer.parseInt(fields[15]);
	}

	public static List<DiseaseGeneRecord> parseDiseaseGeneRecords(String tsvFilePath) throws IOException {
		return Files.readAllLines(Paths.get(tsvFilePath))
					.stream()
					.skip(1) // skip header
					.map(DiseaseGeneRecord::new)
					.collect(Collectors.toList());
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

	public List<String> getVariantId() {
		return variantId;
	}

	public String getDisease() {
		return disease;
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

	public List<Long> getCosmicPubMedIds() {
		return cosmicPubMedIds;
	}

	public String getCurator() {
		return curator;
	}

	public String getConsequence() {
		return consequence;
	}

	public String getNormalReaction() {
		return normalReaction;
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

	private static List<String> getListFromCSVString(String csvLine) {
		return Arrays.asList(csvLine.split(","));
	}

	private static List<Long> convertStringListToLongList(List<String> stringList) {
		return stringList.stream().map(Long::parseLong).collect(Collectors.toList());
	}
}
