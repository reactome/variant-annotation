package org.reactome.curation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.reactome.curation.ParseUtilities.equalOrBothNull;

public class Main {

	public static void main(String[] args) throws IOException {
		final String tsvDir = "src/main/resources";
		final String outputFile = "merged.txt";
		final String errorFile = "merged.err";

		Files.deleteIfExists(Paths.get(outputFile));
		Files.deleteIfExists(Paths.get(errorFile));

		List<String> outputHeaders = Arrays.asList("Protein", "OMIM_identifier", "UniProt_ID", "Variant_name",
												   "Variant_ID", "Disease", "Mutation_AA", "GOF_LOF_null",
												   "WT_Reactome_Pathway", "Selected_pubmed_PMID", "COSMIC_Pubmed_PMID",
												   "Curator", "Consequence", "NormalReaction", "Comments", "Status",
												   "ReleaseVersion", "Protein_in_Reactome", "Any_Variants_Annotated");

		List<DiseaseGeneRecord> diseaseGeneRecords = DiseaseGeneRecord.parseDiseaseGeneRecords(
			Paths.get(tsvDir, "DiseaseGenes.tsv").toString());
		List<HighPriorityVariantRecord> allHighPriorityVariantRecords = HighPriorityVariantRecord.parseHighPriorityVariantRecords(
			Paths.get(tsvDir, "HighPriorityVariants5.tsv").toString());
		List<AbridgedCosmicRecord> allAbridgedCosmicRecords = AbridgedCosmicRecord.parseAbridgedCosmicRecords(
			Paths.get(tsvDir, "Abridged-NoPubMed.tsv").toString());

		Map<String, List<HighPriorityVariantRecord>> variantNameToHighPriorityVariantRecords = allHighPriorityVariantRecords
			.stream()
			.collect(Collectors.groupingBy(HighPriorityVariantRecord::getVariantName));

		Map<String, List<AbridgedCosmicRecord>> variantNameToAbridgedCosmicRecords = allAbridgedCosmicRecords.stream().collect(Collectors.groupingBy(AbridgedCosmicRecord::getVariantName));


		printToFile(String.join("\t", outputHeaders).concat(System.lineSeparator()), outputFile);
		for (DiseaseGeneRecord diseaseGeneRecord : diseaseGeneRecords) {
			List<HighPriorityVariantRecord> highPriorityVariantRecords = variantNameToHighPriorityVariantRecords.computeIfAbsent(
				diseaseGeneRecord.getVariantName(), k -> new ArrayList<>());

			List<AbridgedCosmicRecord> abridgedCosmicRecords = variantNameToAbridgedCosmicRecords.computeIfAbsent(
				diseaseGeneRecord.getVariantName(), k -> new ArrayList<>());

			if (abridgedCosmicRecords.isEmpty()) {
				AdditionalAnnotations additionalAnnotations = new AdditionalAnnotations.Builder()
					.isProteinInReactome(getIsProteinInReactome(highPriorityVariantRecords))
					.build();
				printToFile(createOutputLine(diseaseGeneRecord, additionalAnnotations), outputFile);
			} else {
				for (AbridgedCosmicRecord abridgedCosmicRecord : abridgedCosmicRecords) {
					List<String> mismatches = getMismatches(
						abridgedCosmicRecord, diseaseGeneRecord, highPriorityVariantRecords
					);

					if (!mismatches.isEmpty()) {
						for (String mismatchError : mismatches) {
							printToFile(mismatchError, errorFile);
						}
						continue;
					}

					AdditionalAnnotations additionalAnnotations = new AdditionalAnnotations.Builder()
						.withVariantId(abridgedCosmicRecord.getVariantId())
						.withMutationAA(abridgedCosmicRecord.getMutationAA())
						.withCosmicPubMedId(abridgedCosmicRecord.getCosmicPubMedId())
						.isProteinInReactome(abridgedCosmicRecord.proteinIsInReactome())
						.areAnyVariantsAnnotated(abridgedCosmicRecord.anyVariantsAreAnnotated())
						.build();


					printToFile(createOutputLine(diseaseGeneRecord, additionalAnnotations), outputFile);
				}
			}
		}
	}

	private static String createOutputLine(DiseaseGeneRecord diseaseGeneRecord,
										   AdditionalAnnotations additionalAnnotations) {
		return String.join(
			"\t",
			diseaseGeneRecord.getProtein(),
			diseaseGeneRecord.getOmimIdentifier(),
			diseaseGeneRecord.getUniprotId(),
			diseaseGeneRecord.getVariantName(),
			additionalAnnotations.getVariantId(),
			diseaseGeneRecord.getDiseaseAsString(),
			additionalAnnotations.getMutationAA(),
			diseaseGeneRecord.getGofLofNull(),
			diseaseGeneRecord.getWtReactomePathway(),
			diseaseGeneRecord.getSelectedPubMedIdsAsString(),
			additionalAnnotations.getCosmicPubMedIdAsString(),
			diseaseGeneRecord.getCurator(),
			diseaseGeneRecord.getConsequenceAsString(),
			diseaseGeneRecord.getNormalReactionAsString(),
			diseaseGeneRecord.getComments(),
			diseaseGeneRecord.getStatus(),
			diseaseGeneRecord.getReleaseVersionAsString(),
			additionalAnnotations.getIsProteinInReactomeAsString(),
			additionalAnnotations.getAreAnyVariantsAnnotatedAsString()
		).concat(System.lineSeparator());
	}

	private static List<String> getMismatches(
		AbridgedCosmicRecord abridgedCosmicRecord,
		DiseaseGeneRecord diseaseGeneRecord,
		List<HighPriorityVariantRecord> highPriorityVariantRecords
	) {
		List<String> mismatches = new ArrayList<>();

		if (abridgedCosmicRecord.getReleaseVersion() != diseaseGeneRecord.getReleaseVersion()) {
			mismatches.add(diseaseGeneRecord.getProtein() + " in disease gene records with variant " +
				diseaseGeneRecord.getVariantName() + " has a release version of '" +
				diseaseGeneRecord.getReleaseVersionAsString() + "' that does not match the abridged cosmic record's " +
				"release version of '" + abridgedCosmicRecord.getReleaseVersionAsString() + "'" + System.lineSeparator()
			);
		}

		if (!abridgedCosmicRecord.getStatus().isEmpty() &&
			!abridgedCosmicRecord.getStatus().equals(diseaseGeneRecord.getStatus())
		) {
			mismatches.add(diseaseGeneRecord.getProtein() + " in disease gene records with variant " +
				diseaseGeneRecord.getVariantName() + " has a status of '" + diseaseGeneRecord.getStatus() + "' that " +
				" not match the abridged cosmic record's status of '" + abridgedCosmicRecord.getStatus() + "'" +
				System.lineSeparator()
			);
		}

		if (!highPriorityVariantRecords.stream().allMatch(
			hpvRecord -> equalOrBothNull(hpvRecord.proteinIsInReactome(), abridgedCosmicRecord.proteinIsInReactome())
		)) {
			mismatches.add(abridgedCosmicRecord.getVariantName() + " in abridged cosmic records has a value for " +
				"'proteins in reactome' that does not match those in the high priority variant records" +
				System.lineSeparator()
			);
		}

		return mismatches;
	}

	private static Boolean getIsProteinInReactome(List<HighPriorityVariantRecord> highPriorityVariantRecords) {
		if (highPriorityVariantRecords.size() > 1) {
			System.out.println(highPriorityVariantRecords.toString());
		}

		if (highPriorityVariantRecords.isEmpty() ||
			highPriorityVariantRecords.stream().allMatch(r -> r.proteinIsInReactome() == null)) {
			return null;
		}

		return highPriorityVariantRecords.stream().anyMatch(HighPriorityVariantRecord::proteinIsInReactome);
	}

	private static void printToFile(String output, String filePath) throws IOException {
		Files.write(Paths.get(filePath), output.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}
}
