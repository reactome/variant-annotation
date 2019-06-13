package org.reactome.curation;

public class AdditionalAnnotations {
	private Boolean proteinInReactome;
	private String variantId;
	private String mutationAA;
	private Integer cosmicPubMedId;
	private Boolean areAnyVariantsAnnotated;

	private AdditionalAnnotations() {

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

	public String getVariantId() {
		return variantId == null ? "" : variantId;
	}

	public String getMutationAA() {
		return mutationAA == null ? "" : mutationAA;
	}

	public String getCosmicPubMedIdAsString() {
		return cosmicPubMedId == null ? "" : cosmicPubMedId.toString();
	}

	public Integer getCosmicPubMedId() {
		return cosmicPubMedId;
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
		private Boolean proteinInReactome;
		private String variantId;
		private String mutationAA;
		private Integer cosmicPubMedId;
		private Boolean areAnyVariantsAnnotated;

		public Builder() {

		}

		public Builder isProteinInReactome(Boolean proteinInReactome) {
			this.proteinInReactome = proteinInReactome;

			return this;
		}

		public Builder withVariantId(String variantId) {
			this.variantId = variantId;

			return this;
		}

		public Builder withMutationAA(String mutationAA) {
			this.mutationAA = mutationAA;

			return this;
		}

		public Builder withCosmicPubMedId(int cosmicPubMedId) {
			this.cosmicPubMedId = cosmicPubMedId;

			return this;

		}

		public Builder areAnyVariantsAnnotated(Boolean areAnyVariantsAnnotated) {
			this.areAnyVariantsAnnotated = areAnyVariantsAnnotated;

			return this;
		}

		public AdditionalAnnotations build() {
			AdditionalAnnotations annotations = new AdditionalAnnotations();
			annotations.proteinInReactome = this.proteinInReactome;
			annotations.variantId = this.variantId;
			annotations.mutationAA = this.mutationAA;
			annotations.cosmicPubMedId = this.cosmicPubMedId;
			annotations.areAnyVariantsAnnotated = this.areAnyVariantsAnnotated;

			return annotations;
		}
	}
}
