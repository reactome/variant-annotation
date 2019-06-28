package org.reactome.curation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static org.reactome.curation.AbridgedCosmicRecord.allSameOtherThanVariantAndCosmicPubMedId;
import static org.reactome.curation.AbridgedCosmicRecord.getCosmicPubMedIds;
import static org.reactome.curation.AbridgedCosmicRecord.getVariantIds;
import static org.reactome.curation.HighPriorityVariantRecord.getIsProteinInReactome;
import static org.reactome.curation.ParseUtilities.equalOrBothNull;

public class Main {
	private static final String tsvDir = "src/main/resources";
	private static final String outputFile = "merged.txt";
	private static final String errorFile = "merged.err";

	public static void main(String[] args) throws IOException {
		Files.deleteIfExists(Paths.get(outputFile));
		Files.deleteIfExists(Paths.get(errorFile));

		List<DiseaseGeneRecord> diseaseGeneRecords = DiseaseGeneRecord.parseDiseaseGeneRecords(
			Paths.get(tsvDir, "DiseaseGenes.tsv").toString()
		);
		List<HighPriorityVariantRecord> allHighPriorityVariantRecords = HighPriorityVariantRecord.parseHighPriorityVariantRecords(
			Paths.get(tsvDir, "HighPriorityVariants5.tsv").toString()
		);

		Map<String, List<AbridgedCosmicRecord>> variantNameToAbridgedCosmicRecords =
			AbridgedCosmicRecord.parseAbridgedCosmicRecords(
				Paths.get(tsvDir, "Abridged-NoPubMed.tsv").toString()
			)
			.stream()
			.collect(Collectors.groupingBy(AbridgedCosmicRecord::getVariantName));

		printOutputHeaders();
		printOutputLines(diseaseGeneRecords, allHighPriorityVariantRecords, variantNameToAbridgedCosmicRecords);
	}

	private static void printOutputHeaders() {
		List<String> outputHeaders = Arrays.asList("Protein", "OMIM_identifier", "UniProt_ID", "Variant_name",
			"Variant_ID", "Disease", "Mutation_AA", "GOF_LOF_null",
			"WT_Reactome_Pathway", "Selected_pubmed_PMID", "COSMIC_Pubmed_PMID",
			"Curator", "Consequence", "NormalReaction", "Comments", "Status",
			"ReleaseVersion", "Protein_in_Reactome", "Any_Variants_Annotated")
		;

		printToFile(String.join("\t", outputHeaders).concat(lineSeparator()), outputFile);
	}

	private static void printOutputLines(
		List<DiseaseGeneRecord> diseaseGeneRecords,
		List<HighPriorityVariantRecord> allHighPriorityVariantRecords,
		Map<String, List<AbridgedCosmicRecord>> variantNameToAbridgedCosmicRecords
	) {
		Map<String, String> variantNameToOutputLine = new LinkedHashMap<>();
		variantNameToOutputLine.putAll(
			getVariantNameToOutputLines(
				diseaseGeneRecords, allHighPriorityVariantRecords, variantNameToAbridgedCosmicRecords
			)
		);
		variantNameToOutputLine.putAll(
			getVariantNameToOutputLines(allHighPriorityVariantRecords, variantNameToAbridgedCosmicRecords)
		);

		for (String outputLine : variantNameToOutputLine.values()) {
			printToFile(outputLine, outputFile);
		}
	}

	private static Map<String, String> getVariantNameToOutputLines(
		List<DiseaseGeneRecord> diseaseGeneRecords,
		List<HighPriorityVariantRecord> allHighPriorityVariantRecords,
		Map<String, List<AbridgedCosmicRecord>> variantNameToAbridgedCosmicRecords
	) {
		Map<String, String> variantNameToOutputLine = new LinkedHashMap<>();

		Map<String, List<HighPriorityVariantRecord>> variantNameToHighPriorityVariantRecords =
			allHighPriorityVariantRecords
			.stream()
			.collect(Collectors.groupingBy(HighPriorityVariantRecord::getVariantName));

		for (DiseaseGeneRecord diseaseGeneRecord : diseaseGeneRecords) {
			List<HighPriorityVariantRecord> highPriorityVariantRecords = variantNameToHighPriorityVariantRecords.computeIfAbsent(
				diseaseGeneRecord.getVariantName(), k -> new ArrayList<>()
			);

			List<AbridgedCosmicRecord> abridgedCosmicRecords = variantNameToAbridgedCosmicRecords.computeIfAbsent(
				diseaseGeneRecord.getVariantName(), k -> new ArrayList<>()
			);

			if (abridgedCosmicRecords.isEmpty()) {
				CommonAnnotations commonAnnotations = new CommonAnnotations.Builder()
					.withProtein(diseaseGeneRecord.getProtein())
					.withVariantName(diseaseGeneRecord.getVariantName())
					.withStatus(diseaseGeneRecord.getStatus())
					.withReleaseVersion(diseaseGeneRecord.getReleaseVersion())
					.isProteinInReactome(getIsProteinInReactome(highPriorityVariantRecords))
					.build();
				variantNameToOutputLine.put(
					diseaseGeneRecord.getVariantName(),
					createOutputLine(diseaseGeneRecord, commonAnnotations)
				);
			} else {
				List<String> errors = getErrors(abridgedCosmicRecords, diseaseGeneRecord, highPriorityVariantRecords);
				if (!errors.isEmpty()) {
					printToFile(errors, errorFile);
					continue;
				}

				AbridgedCosmicRecord representativeAbridgedCosmicRecord = abridgedCosmicRecords.get(0);
				CommonAnnotations commonAnnotations = new CommonAnnotations.Builder()
					.withProtein(diseaseGeneRecord.getProtein())
					.withVariantName(diseaseGeneRecord.getVariantName())
					.withVariantIds(getVariantIds(abridgedCosmicRecords))
					.withMutationAA(representativeAbridgedCosmicRecord.getMutationAA())
					.withCosmicPubMedIds(getCosmicPubMedIds(abridgedCosmicRecords))
					.withStatus(diseaseGeneRecord.getStatus())
					.withReleaseVersion(diseaseGeneRecord.getReleaseVersion())
					.isProteinInReactome(representativeAbridgedCosmicRecord.proteinIsInReactome())
					.areAnyVariantsAnnotated(representativeAbridgedCosmicRecord.anyVariantsAreAnnotated())
					.build();

				variantNameToOutputLine.put(
					diseaseGeneRecord.getVariantName(),
					createOutputLine(diseaseGeneRecord, commonAnnotations)
				);

			}
		}

		return variantNameToOutputLine;
	}

	private static Map<String, String> getVariantNameToOutputLines(
		List<HighPriorityVariantRecord> allHighPriorityVariantRecords,
		Map<String, List<AbridgedCosmicRecord>> variantNameToAbridgedCosmicRecords
	) {
		Map<String, String> variantNameToOutputLine = new LinkedHashMap<>();

		for (HighPriorityVariantRecord highPriorityVariantRecord : allHighPriorityVariantRecords) {
			List<AbridgedCosmicRecord> abridgedCosmicRecords = variantNameToAbridgedCosmicRecords.computeIfAbsent(
				highPriorityVariantRecord.getVariantName(), k -> new ArrayList<>()
			);

			List<String> errors = getErrors(abridgedCosmicRecords, highPriorityVariantRecord);
			if (!errors.isEmpty()) {
				printToFile(errors, errorFile);
				continue;
			}

			AbridgedCosmicRecord representativeAbridgedCosmicRecord = abridgedCosmicRecords.get(0);
			CommonAnnotations commonAnnotations = new CommonAnnotations.Builder()
				.withProtein(representativeAbridgedCosmicRecord.getProtein())
				.withVariantName(representativeAbridgedCosmicRecord.getVariantName())
				.withVariantIds(getVariantIds(abridgedCosmicRecords))
				.withMutationAA(representativeAbridgedCosmicRecord.getMutationAA())
				.withCosmicPubMedIds(getCosmicPubMedIds(abridgedCosmicRecords))
				.withStatus(representativeAbridgedCosmicRecord.getStatus())
				.withReleaseVersion(representativeAbridgedCosmicRecord.getReleaseVersion())
				.isProteinInReactome(representativeAbridgedCosmicRecord.proteinIsInReactome())
				.areAnyVariantsAnnotated(representativeAbridgedCosmicRecord.anyVariantsAreAnnotated())
				.build();

			variantNameToOutputLine.put(
				highPriorityVariantRecord.getVariantName(),
				createOutputLine(commonAnnotations)
			);
		}

		return variantNameToOutputLine;
	}

	private static List<String> getErrors(
		List<AbridgedCosmicRecord> abridgedCosmicRecords,
		HighPriorityVariantRecord highPriorityVariantRecord
	) {
		List<String> errors = new ArrayList<>();

		if (abridgedCosmicRecords.isEmpty()) {
			String error = highPriorityVariantRecord.getVariantName() + " does not have any corresponding " +
				"abridged cosmic record(s)" + lineSeparator();

			errors.add(error);
		}

		String abridgedCosmicRecordDifferencesError =
			checkForAbridgedCosmicRecordDifferencesError(highPriorityVariantRecord.getVariantName(), abridgedCosmicRecords);

		if (!abridgedCosmicRecordDifferencesError.isEmpty()) {
			errors.add(abridgedCosmicRecordDifferencesError);
		}

		AbridgedCosmicRecord representativeAbridgedCosmicRecord = abridgedCosmicRecords.get(0);
		List<String> mismatches = getMismatches(highPriorityVariantRecord, representativeAbridgedCosmicRecord);
		if (!mismatches.isEmpty()) {
			errors.addAll(mismatches);
		}

		return errors;
	}

	private static List<String> getErrors(
		List<AbridgedCosmicRecord> abridgedCosmicRecords,
		DiseaseGeneRecord diseaseGeneRecord,
		List<HighPriorityVariantRecord> highPriorityVariantRecords
	) {
		List<String> errors = new ArrayList<>();

		String abridgedCosmicRecordDifferencesError =
			checkForAbridgedCosmicRecordDifferencesError(diseaseGeneRecord.getVariantName(), abridgedCosmicRecords);

		if (!abridgedCosmicRecordDifferencesError.isEmpty()) {
			errors.add(abridgedCosmicRecordDifferencesError);
		}

		AbridgedCosmicRecord representativeAbridgedCosmicRecord = abridgedCosmicRecords.get(0);
		List<String> mismatches = getMismatches(
			representativeAbridgedCosmicRecord, diseaseGeneRecord, highPriorityVariantRecords
		);
		if (!mismatches.isEmpty()) {
			errors.addAll(mismatches);
		}

		return errors;
	}

	private static String checkForAbridgedCosmicRecordDifferencesError(
		String variantName,
		List<AbridgedCosmicRecord> abridgedCosmicRecords
	) {
		if (!allSameOtherThanVariantAndCosmicPubMedId(abridgedCosmicRecords)) {
			return variantName + " has abridged cosmic records with " +
				"differences (omitted from merged output)" + lineSeparator();
		} else {
			return "";
		}
	}

	private static String createOutputLine(DiseaseGeneRecord diseaseGeneRecord, CommonAnnotations commonAnnotations) {
		return String.join(
			"\t",
			commonAnnotations.getProtein(),
			diseaseGeneRecord != null ? diseaseGeneRecord.getOmimIdentifier() : "",
			diseaseGeneRecord != null ? diseaseGeneRecord.getUniprotId() : "",
			commonAnnotations.getVariantName(),
			commonAnnotations.getVariantIdsAsString(),
			diseaseGeneRecord != null ? diseaseGeneRecord.getDiseaseAsString() : "",
			commonAnnotations.getMutationAA(),
			diseaseGeneRecord != null ? diseaseGeneRecord.getGofLofNull() : "",
			diseaseGeneRecord != null ? diseaseGeneRecord.getWtReactomePathway() : "",
			diseaseGeneRecord != null ? diseaseGeneRecord.getSelectedPubMedIdsAsString() : "",
			commonAnnotations.getCosmicPubMedIdsAsString(),
			diseaseGeneRecord != null ? diseaseGeneRecord.getCurator() : "",
			diseaseGeneRecord != null ? diseaseGeneRecord.getConsequenceAsString() : "",
			diseaseGeneRecord != null ? diseaseGeneRecord.getNormalReactionAsString() : "",
			diseaseGeneRecord != null ? diseaseGeneRecord.getComments() : "",
			commonAnnotations.getStatus(),
			commonAnnotations.getReleaseVersionAsString(),
			commonAnnotations.getIsProteinInReactomeAsString(),
			commonAnnotations.getAreAnyVariantsAnnotatedAsString()
		).concat(lineSeparator());
	}

	private static String createOutputLine(CommonAnnotations commonAnnotations) {
		return createOutputLine(null, commonAnnotations);
	}

	private static List<String> getMismatches(
		AbridgedCosmicRecord abridgedCosmicRecord,
		DiseaseGeneRecord diseaseGeneRecord,
		List<HighPriorityVariantRecord> highPriorityVariantRecords
	) {
		List<String> mismatches = new ArrayList<>();

		mismatches.addAll(getMismatches(diseaseGeneRecord, abridgedCosmicRecord));
		mismatches.addAll(getMismatches(highPriorityVariantRecords, abridgedCosmicRecord));

		return mismatches;
	}

	private static List<String> getMismatches(DiseaseGeneRecord diseaseGeneRecord,
										AbridgedCosmicRecord abridgedCosmicRecord) {
		List<String> mismatches = new ArrayList<>();

		if (!abridgedCosmicRecord.getReleaseVersionAsString().isEmpty() &&
			abridgedCosmicRecord.getReleaseVersion() != diseaseGeneRecord.getReleaseVersion()) {
			mismatches.add(diseaseGeneRecord.getProtein() + " in disease gene records with variant " +
				diseaseGeneRecord.getVariantName() + " has a release version of '" +
				diseaseGeneRecord.getReleaseVersionAsString() + "' that does not match the abridged cosmic record's " +
				"release version of '" + abridgedCosmicRecord.getReleaseVersionAsString() + "'" + lineSeparator()
			);
		}

		if (!abridgedCosmicRecord.getStatus().isEmpty() &&
			!abridgedCosmicRecord.getStatus().equals(diseaseGeneRecord.getStatus())
		) {
			mismatches.add(diseaseGeneRecord.getProtein() + " in disease gene records with variant " +
				diseaseGeneRecord.getVariantName() + " has a status of '" + diseaseGeneRecord.getStatus() + "' that " +
				" not match the abridged cosmic record's status of '" + abridgedCosmicRecord.getStatus() + "'" +
				lineSeparator()
			);
		}

		return mismatches;
	}

	private static List<String> getMismatches(
		List<HighPriorityVariantRecord> highPriorityVariantRecords,
		AbridgedCosmicRecord abridgedCosmicRecord
	) {
		List<String> mismatches = new ArrayList<>();

		return highPriorityVariantRecords
			.stream()
			.map(hpvRecord -> getMismatches(hpvRecord, abridgedCosmicRecord))
			.flatMap(Collection::stream)
			.distinct()
			.collect(Collectors.toList());
	}

	private static List<String> getMismatches(HighPriorityVariantRecord highPriorityVariantRecord,
									   AbridgedCosmicRecord abridgedCosmicRecord) {
		List<String> mismatches = new ArrayList<>();

		if (!equalOrBothNull(highPriorityVariantRecord.proteinIsInReactome(), abridgedCosmicRecord.proteinIsInReactome())) {
			mismatches.add(
				getProteinInReactomeMisMatchError(abridgedCosmicRecord, highPriorityVariantRecord)
			);
		}

		return mismatches;
	}

	private static String getProteinInReactomeMisMatchError(
		AbridgedCosmicRecord abridgedCosmicRecord,
		HighPriorityVariantRecord highPriorityVariantRecord
	) {
		return abridgedCosmicRecord.getVariantName() + " in abridged cosmic records has a value for " +
			"'proteins in reactome' of " + abridgedCosmicRecord.getIsProteinInReactomeAsString() + " that does not " +
			"match the value in the high priority variant record of " +
			highPriorityVariantRecord.getIsProteinInReactomeAsString() + lineSeparator();
	}

	private static void printToFile(List<String> outputStrings, String filePath) {
		for (String outputString : outputStrings) {
			printToFile(outputString.concat(lineSeparator()), filePath);
		}
	}

	private static void printToFile(String outputString, String filePath) {
		try {
			Files.write(Paths.get(filePath), outputString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
