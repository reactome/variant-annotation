package org.reactome.curation;

import java.util.List;
import java.util.stream.Collectors;

public class AdditionalAnnotations {
	private String protein;
	private Boolean proteinInReactome;
	private List<String> variantIds;
	private String mutationAA;
	private List<Integer> cosmicPubMedIds;
	private String status;
	private int releaseVersion;
	private Boolean areAnyVariantsAnnotated;

	private AdditionalAnnotations() {

	}

	public String getProtein() {
		return protein;
	}

	public String getIsProteinInReactomeAsString() {
		if (proteinInReactome == null) {
			return "";
		} else {
			return proteinInReactome ? "yes" : "no";
		}
	}

	public Boolean proteinIsInReactome() {
		return proteinInReactome;
	}

	public String getVariantIdsAsString() {
		if (variantIds == null || variantIds.isEmpty()) {
			return "";
		}

		return variantIds
			.stream()
			.distinct()
			.collect(Collectors.joining(","));
	}

	public List<String> getVariantIds() {
		return variantIds;
	}

	public String getMutationAA() {
		return mutationAA == null ? "" : mutationAA;
	}

	public String getCosmicPubMedIdsAsString() {
		if (cosmicPubMedIds == null || cosmicPubMedIds.isEmpty()) {
			return "";
		}

		return cosmicPubMedIds
			.stream()
			.distinct()
			.map(Object::toString)
			.collect(Collectors.joining(","));
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

	public List<Integer> getCosmicPubMedIds() {
		return cosmicPubMedIds;
	}

	public String getAreAnyVariantsAnnotatedAsString() {
		if (areAnyVariantsAnnotated == null) {
			return "";
		} else {
			return areAnyVariantsAnnotated ? "yes" : "no";
		}
	}

	public Boolean anyVariantsAreAnnotated() {
		return areAnyVariantsAnnotated;
	}

	public static class Builder {
		private String protein;
		private Boolean proteinInReactome;
		private List<String> variantIds;
		private String mutationAA;
		private List<Integer> cosmicPubMedIds;
		private String status;
		private int releaseVersion;
		private Boolean areAnyVariantsAnnotated;

		public Builder() {

		}

		public Builder withProtein(String protein) {
			this.protein = protein;

			return this;
		}

		public Builder isProteinInReactome(Boolean proteinInReactome) {
			this.proteinInReactome = proteinInReactome;

			return this;
		}

		public Builder withVariantIds(List<String> variantIds) {
			this.variantIds = variantIds;

			return this;
		}

		public Builder withMutationAA(String mutationAA) {
			this.mutationAA = mutationAA;

			return this;
		}

		public Builder withCosmicPubMedIds(List<Integer> cosmicPubMedIds) {
			this.cosmicPubMedIds = cosmicPubMedIds;

			return this;

		}

		public Builder withStatus(String status) {
			this.status = status;

			return this;
		}

		public Builder withReleaseVersion(int releaseVersion) {
			this.releaseVersion = releaseVersion;

			return this;
		}

		public Builder areAnyVariantsAnnotated(Boolean areAnyVariantsAnnotated) {
			this.areAnyVariantsAnnotated = areAnyVariantsAnnotated;

			return this;
		}

		public AdditionalAnnotations build() {
			AdditionalAnnotations annotations = new AdditionalAnnotations();

			annotations.protein = protein;
			annotations.proteinInReactome = this.proteinInReactome;
			annotations.variantIds = this.variantIds;
			annotations.mutationAA = this.mutationAA;
			annotations.cosmicPubMedIds = this.cosmicPubMedIds;
			annotations.status = this.status;
			annotations.releaseVersion = this.releaseVersion;
			annotations.areAnyVariantsAnnotated = this.areAnyVariantsAnnotated;

			return annotations;
		}
	}
}
