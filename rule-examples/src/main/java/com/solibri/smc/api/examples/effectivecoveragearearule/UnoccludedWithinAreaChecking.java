package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.solibri.geometry.primitive2d.MArea;
import com.solibri.smc.api.model.Component;

class UnoccludedWithinAreaChecking extends EffectiveCoverageChecking {

	public UnoccludedWithinAreaChecking(
		Component spaceEntity,
		Collection<Component> effectSources,
		double effectRange,
		double minimumCoverage) {
		super(spaceEntity, effectSources, effectRange, minimumCoverage);
	}

	@Override
	public Optional<CoverageAreaViolation> checkViolations() {
		/* Retrieve the footprint of the space and calculate the total area */
		calculateTotalArea();

		/*
		 * Find the intersecting effect sources, resize their footprints, build
		 * the union of the areas and intersect it with the space footprint.
		 */
		calculateEffectArea(getIntersectingEffectSources());

		/*
		 * If the coverage area is below the minimum coverage threshold, return
		 * a violation.
		 */
		return checkCoverage();
	}

	Set<Component> getIntersectingEffectSources() {
		return effectSources.stream().filter(source -> intersectsSpace(spaceEntity, source))
			.collect(Collectors.toSet());
	}

	boolean intersectsSpace(Component spaceEntity, Component sourceEntity) {
		return spaceEntity.getIntersections(sourceEntity)
			.stream()
			.anyMatch((intersection) -> !intersection.isEmpty());
	}

	@Override
	MArea calculateCoverage(MArea sourceFootprint) {
		MArea footprintCopy = MArea.create(sourceFootprint);
		footprintCopy.resize(effectRange, true);
		return footprintCopy;
	}
}
