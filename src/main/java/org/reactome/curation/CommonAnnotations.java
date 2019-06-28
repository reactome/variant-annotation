package org.reactome.curation;

import java.util.List;
import java.util.stream.Collectors;

public class CommonAnnotations {
	private String recordLine;

	private String protein;
	private String variantName;
	private Boolean proteinInReactome;
	private List<String> variantIds;
	private String mutationAA;
	private List<Long> cosmicPubMedIds;
	private String status;
	private int releaseVersion;
	private Boolean areAnyVariantsAnnotated;

	private CommonAnnotations() {

	}

	public String getRecordLine() {
		return this.recordLine;
	}

	public String getProtein() {
		return this.protein;
	}

	public String getIsProteinInReactomeAsString() {
		if (proteinIsInReactome() == null) {
			return "";
		} else {
			return proteinIsInReactome() ? "yes" : "no";
		}
	}

	public String getVariantName() {
		return this.variantName;
	}

	public Boolean proteinIsInReactome() {
		return this.proteinInReactome;
	}

	public String getVariantIdsAsString() {
		if (getVariantIds() == null || getVariantIds().isEmpty()) {
			return "";
		}

		return getVariantIds()
			.stream()
			.distinct()
			.collect(Collectors.joining(","));
	}

	public List<String> getVariantIds() {
		return this.variantIds;
	}

	public String getMutationAA() {
		return this.mutationAA == null ? "" : this.mutationAA;
	}

	public String getCosmicPubMedIdsAsString() {
		if (getCosmicPubMedIds() == null || getCosmicPubMedIds().isEmpty()) {
			return "";
		}

		return getCosmicPubMedIds()
			.stream()
			.distinct()
			.map(Object::toString)
			.collect(Collectors.joining(","));
	}

	public String getStatus() {
		return this.status;
	}

	public int getReleaseVersion() {
		return this.releaseVersion;
	}

	public String getReleaseVersionAsString() {
		if (getReleaseVersion() == -1) {
			return "";
		} else {
			return Integer.toString(getReleaseVersion());
		}
	}

	public List<Long> getCosmicPubMedIds() {
		return this.cosmicPubMedIds;
	}

	public String getAreAnyVariantsAnnotatedAsString() {
		if (anyVariantsAreAnnotated() == null) {
			return "";
		} else {
			return anyVariantsAreAnnotated() ? "yes" : "no";
		}
	}

	public Boolean anyVariantsAreAnnotated() {
		return this.areAnyVariantsAnnotated;
	}

	public static class Builder {
		private String recordLine;

		private String protein;
		private String variantName;
		private Boolean proteinInReactome;
		private List<String> variantIds;
		private String mutationAA;
		private List<Long> cosmicPubMedIds;
		private String status;
		private int releaseVersion;
		private Boolean areAnyVariantsAnnotated;

		public Builder withRecordLine(String recordLine) {
			this.recordLine = recordLine;

			return this;
		}

		public Builder withProtein(String protein) {
			this.protein = protein;

			return this;
		}

		public Builder withVariantName(String variantName) {
			this.variantName = variantName;

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

		public Builder withCosmicPubMedIds(List<Long> cosmicPubMedIds) {
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

		public CommonAnnotations build() {
			CommonAnnotations annotations = new CommonAnnotations();

			annotations.recordLine = this.recordLine;
			annotations.protein = this.protein;
			annotations.variantName = this.variantName;
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
