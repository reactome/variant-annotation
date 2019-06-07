import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AbridgedCosmicRecord {
	private String geneName;
	private String mutationAA;
	private String variantName;
	private List<String> variantId;
	private int cosmicPubMedId;
	private boolean isProteinInReactome;
	private boolean areAnyVariantsAnnotated;
	private boolean highPriority;
	private String status;
	private int releaseVersion;
	private String mutationDescription;
	private List<String> otherVariantsAnnotatedInReactome;

	private AbridgedCosmicRecord(String tsvLine) {
		String[] fields = tsvLine.split("\t");

		this.geneName = fields[0];
		this.mutationAA = fields[1];
		this.variantName = fields[2];
		this.variantId = getListFromCSVString(fields[3]);
		this.cosmicPubMedId = Integer.parseInt(fields[4]);
		this.isProteinInReactome = getBooleanFromYesNo(fields[5]);
		this.areAnyVariantsAnnotated = getBooleanFromYesNo(fields[6]);
		this.highPriority = getBooleanFromYesNo(fields[7]);
		this.status = fields[8];
		this.releaseVersion = Integer.parseInt(fields[9]);
		this.mutationDescription = fields[10];
		this.otherVariantsAnnotatedInReactome = getListFromCSVString(fields[11]);
	}

	public static List<AbridgedCosmicRecord> parseAbridgedCosmicRecords(String tsvFilePath) throws IOException {
		return Files.readAllLines(Paths.get(tsvFilePath))
					.stream()
					.skip(1) // skip header
					.map(AbridgedCosmicRecord::new)
					.collect(Collectors.toList());
	}

	public String getGeneName() {
		return geneName;
	}

	public String getMutationAA() {
		return mutationAA;
	}

	public String getVariantName() {
		return variantName;
	}

	public List<String> getVariantId() {
		return variantId;
	}

	public int getCosmicPubMedId() {
		return cosmicPubMedId;
	}

	public boolean isProteinInReactome() {
		return isProteinInReactome;
	}

	public boolean isAreAnyVariantsAnnotated() {
		return areAnyVariantsAnnotated;
	}

	public boolean isHighPriority() {
		return highPriority;
	}

	public String getStatus() {
		return status;
	}

	public int getReleaseVersion() {
		return releaseVersion;
	}

	public String getMutationDescription() {
		return mutationDescription;
	}

	public List<String> getOtherVariantsAnnotatedInReactome() {
		return otherVariantsAnnotatedInReactome;
	}

	private static List<String> getListFromCSVString(String csvLine) {
		return Arrays.asList(csvLine.split(","));
	}

	private static List<Long> convertStringListToLongList(List<String> stringList) {
		return stringList.stream().map(Long::parseLong).collect(Collectors.toList());
	}

	private boolean getBooleanFromYesNo(String yesOrNo) {
		if (yesOrNo.equalsIgnoreCase("yes")) {
			return true;
		} else if (yesOrNo.equalsIgnoreCase("no")) {
			return false;
		} else {
			throw new IllegalArgumentException("String parameter must be 'yes' or 'no', but received - " + yesOrNo);
		}
	}
}
