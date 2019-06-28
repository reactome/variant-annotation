package org.reactome.curation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
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
				AdditionalAnnotations additionalAnnotations = new AdditionalAnnotations.Builder()
					.isProteinInReactome(getIsProteinInReactome(highPriorityVariantRecords))
					.build();
				variantNameToOutputLine.put(
					diseaseGeneRecord.getVariantName(),
					createOutputLine(diseaseGeneRecord, additionalAnnotations)
				);
			} else {
				if (!abridgedCosmicRecords
					.stream()
					.allMatch(
						abr -> abr.isSameOtherThanVariantAndCosmicPubMedId(abridgedCosmicRecords.get(0))
					)
				) {
					String error = diseaseGeneRecord.getVariantName() + " has abridged cosmic records with " +
								   "differences (omitted from merged output)" + lineSeparator();
					printToFile(error, errorFile);
				} else {
					List<String> variantIds = abridgedCosmicRecords
						.stream()
						.map(AbridgedCosmicRecord::getVariantId)
						.collect(Collectors.toList());

					List<Long> cosmicPubMedIds = abridgedCosmicRecords
						.stream()
						.map(AbridgedCosmicRecord::getCosmicPubMedId)
						.collect(Collectors.toList());

					AbridgedCosmicRecord representativeAbridgedCosmicRecord = abridgedCosmicRecords.get(0);

					List<String> mismatches = getMismatches(
						representativeAbridgedCosmicRecord, diseaseGeneRecord, highPriorityVariantRecords
					);

					if (!mismatches.isEmpty()) {
						for (String mismatchError : mismatches) {
							printToFile(mismatchError, errorFile);
						}
						continue;
					}

					AdditionalAnnotations additionalAnnotations = new AdditionalAnnotations.Builder()
						.withVariantIds(variantIds)
						.withMutationAA(representativeAbridgedCosmicRecord.getMutationAA())
						.withCosmicPubMedIds(cosmicPubMedIds)
						.isProteinInReactome(representativeAbridgedCosmicRecord.proteinIsInReactome())
						.areAnyVariantsAnnotated(representativeAbridgedCosmicRecord.anyVariantsAreAnnotated())
						.build();

					variantNameToOutputLine.put(
						diseaseGeneRecord.getVariantName(),
						createOutputLine(diseaseGeneRecord, additionalAnnotations)
					);
				}
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

			if (abridgedCosmicRecords.isEmpty()) {
				String error = highPriorityVariantRecord.getVariantName() + " does not have any corresponding " +
					"abridged cosmic record(s)" + lineSeparator();
				printToFile(error, errorFile);
				continue;
			}

			if (!abridgedCosmicRecords
				.stream()
				.allMatch(
					abr -> abr.isSameOtherThanVariantAndCosmicPubMedId(abridgedCosmicRecords.get(0))
				)
			) {
				String error = highPriorityVariantRecord.getVariantName() + " has abridged cosmic records with " +
					"differences (omitted from merged output)" + lineSeparator();

				printToFile(error, errorFile);
			} else {
				List<String> variantIds = abridgedCosmicRecords
					.stream()
					.map(AbridgedCosmicRecord::getVariantId)
					.collect(Collectors.toList());

				List<Long> cosmicPubMedIds = abridgedCosmicRecords
					.stream()
					.map(AbridgedCosmicRecord::getCosmicPubMedId)
					.collect(Collectors.toList());

				AbridgedCosmicRecord representativeAbridgedCosmicRecord = abridgedCosmicRecords.get(0);

				List<String> mismatches = getMismatches(
					highPriorityVariantRecord, representativeAbridgedCosmicRecord
				);

				if (!mismatches.isEmpty()) {
					for (String mismatchError : mismatches) {
						printToFile(mismatchError, errorFile);
					}
					continue;
				}

				AdditionalAnnotations additionalAnnotations = new AdditionalAnnotations.Builder()
					.withProtein(representativeAbridgedCosmicRecord.getProtein())
					.withVariantIds(variantIds)
					.withMutationAA(representativeAbridgedCosmicRecord.getMutationAA())
					.withCosmicPubMedIds(cosmicPubMedIds)
					.withStatus(representativeAbridgedCosmicRecord.getStatus())
					.withReleaseVersion(representativeAbridgedCosmicRecord.getReleaseVersion())
					.isProteinInReactome(representativeAbridgedCosmicRecord.proteinIsInReactome())
					.areAnyVariantsAnnotated(representativeAbridgedCosmicRecord.anyVariantsAreAnnotated())
					.build();

				variantNameToOutputLine.put(
					highPriorityVariantRecord.getVariantName(),
					createOutputLine(highPriorityVariantRecord, additionalAnnotations)
				);
			}
		}

		return variantNameToOutputLine;
	}

	private static String createOutputLine(
		DiseaseGeneRecord diseaseGeneRecord,
		AdditionalAnnotations additionalAnnotations
	) {
		return String.join(
			"\t",
			diseaseGeneRecord.getProtein(),
			diseaseGeneRecord.getOmimIdentifier(),
			diseaseGeneRecord.getUniprotId(),
			diseaseGeneRecord.getVariantName(),
			additionalAnnotations.getVariantIdsAsString(),
			diseaseGeneRecord.getDiseaseAsString(),
			additionalAnnotations.getMutationAA(),
			diseaseGeneRecord.getGofLofNull(),
			diseaseGeneRecord.getWtReactomePathway(),
			diseaseGeneRecord.getSelectedPubMedIdsAsString(),
			additionalAnnotations.getCosmicPubMedIdsAsString(),
			diseaseGeneRecord.getCurator(),
			diseaseGeneRecord.getConsequenceAsString(),
			diseaseGeneRecord.getNormalReactionAsString(),
			diseaseGeneRecord.getComments(),
			diseaseGeneRecord.getStatus(),
			diseaseGeneRecord.getReleaseVersionAsString(),
			additionalAnnotations.getIsProteinInReactomeAsString(),
			additionalAnnotations.getAreAnyVariantsAnnotatedAsString()
		).concat(lineSeparator());
	}

	private static String createOutputLine(
		HighPriorityVariantRecord highPriorityVariantRecord,
		AdditionalAnnotations additionalAnnotations
	) {
		if (additionalAnnotations.getReleaseVersion() == 0) {
			System.out.println(highPriorityVariantRecord.getVariantName());
		}

		return String.join(
			"\t",
			additionalAnnotations.getProtein(),
			"",
			"",
			highPriorityVariantRecord.getVariantName(),
			additionalAnnotations.getVariantIdsAsString(),
			"",
			additionalAnnotations.getMutationAA(),
			"",
			"",
			"",
			additionalAnnotations.getCosmicPubMedIdsAsString(),
			"",
			"",
			"",
			"",
			additionalAnnotations.getStatus(),
			additionalAnnotations.getReleaseVersionAsString(),
			highPriorityVariantRecord.getIsProteinInReactomeAsString(),
			additionalAnnotations.getAreAnyVariantsAnnotatedAsString()
		).concat(lineSeparator());
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
			"'proteins in reactome' of " + abridgedCosmicRecord.getIsProteinInReactomeAsString() + "that does not " +
			"match the value in the high priority variant record of " +
			highPriorityVariantRecord.getIsProteinInReactomeAsString() + lineSeparator();
	}

	private static void printToFile(String output, String filePath) {
		try {
			Files.write(Paths.get(filePath), output.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
