package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.Collection;
import java.util.Optional;

import com.solibri.geometry.primitive2d.Area;
import com.solibri.geometry.primitive2d.MArea;
import com.solibri.smc.api.model.Component;

abstract class EffectiveCoverageChecking {
	// init parameters
	final Component spaceEntity;
	final Collection<Component> effectSources;
	final double effectRange;
	final double minimumCoverage;

	// calculated values
	Area spaceArea;
	double total;
	MArea coverage;
	double coverageArea;
	double coverageRatio;

	EffectiveCoverageChecking(
		Component spaceEntity,
		Collection<Component> effectSources,
		double effectRange,
		double minimumCoverage) {
		this.spaceEntity = spaceEntity;
		this.effectSources = effectSources;
		this.effectRange = effectRange;
		this.minimumCoverage = minimumCoverage;
	}

	Optional<CoverageAreaViolation> checkViolations() {
		/* Retrieve the footprint of the space */
		calculateTotalArea();

		/*
		 * Retrieve the effect source footprints, resize them, build the union
		 * of the areas and intersect it with the space footprint.
		 */
		calculateEffectArea();

		/*
		 * If the coverage area is below the minimum coverage threshold, return
		 * a violation.
		 */
		return checkCoverage();
	}

	abstract MArea calculateCoverage(MArea sourceFootprint);

	void calculateTotalArea() {
		spaceArea = spaceEntity.getFootprint().getArea();
		total = spaceArea.getSize();
	}

	void calculateEffectArea() {
		calculateEffectArea(effectSources);
	}

	/*
	 * Calculate the coverage from effect sources.
	 *
	 * This method calculates the footprint of each effect source and applies
	 * the coverage calculation to it. The coverage calculation is implemented
	 * in the subclasses.
	 *
	 * A union is taken from the resulting coverages from each effect source and
	 * the resulting coverage is the common area between the space footprint and
	 * the union of the coverage areas.
	 *
	 */
	void calculateEffectArea(Collection<Component> effectSources) {
		final MArea accumulatedArea = MArea.create();

		effectSources.stream()
			.map(entity -> MArea.create(entity.getFootprint().getArea()))
			.map(this::calculateCoverage)
			.forEach((sourceArea) -> accumulatedArea.add(sourceArea));

		coverage = accumulatedArea;
		coverage.intersect(spaceArea);
		coverageArea = coverage.getSize();
	}

	Optional<CoverageAreaViolation> checkCoverage() {
		coverageRatio = coverageArea / total;
		if (coverageRatio < minimumCoverage) {
			return Optional.of(new CoverageAreaViolation(
				spaceEntity,
				effectSources,
				coverage,
				coverageArea,
				coverageRatio,
				minimumCoverage));
		}
		return Optional.empty();
	}
}
