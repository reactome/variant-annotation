import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) throws IOException {
		final String tsvDir = "src/main/resources";

		List<String> outputHeaders = Arrays.asList(
			"Protein",
			"OMIM_identifier",
			"UniProt_ID",
			"Variant_name",
			"Mutation_ID",
			"Disease",
			"Mutation_AA",
			"GOF_LOF_null",
			"WT_Reactome_Pathway",
			"Selected_pubmed_PMID",
			"COSMIC_Pubmed_PMID",
			"Curator",
			"Consequence",
			"NormalReaction",
			"Comments",
			"Status",
			"ReleaseVersion",
			"Protein_in_Reactome",
			"Any_Variants_Annotated"
		);

		//DiseaseGeneRecord.parseDiseaseGeneRecords(Paths.get(tsvDir, "DiseaseGenes.tsv").toString());
		HighPriorityVariantRecord.parseHighPriorityVariantRecords(Paths.get(tsvDir, "HighPriorityVariants5.tsv").toString());
		//AbridgedCosmicRecord.parseAbridgedCosmicRecords(Paths.get(tsvDir, "Abridged-NoPubMed.tsv").toString());
	}
}
