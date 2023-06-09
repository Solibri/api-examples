package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.Collection;

import com.solibri.geometry.primitive2d.MArea;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.examples.effectivecoveragearearule.visibility.VisibilityPolygonCalculator;

/**
 * UnoccludedWithinAreaChecking implements the Effective coverage area type 4
 * behaviour.
 *
 * It utilizes only sources that intersect the space and from each point in the
 * coverage area, there is a line of sight to the effect source.
 */
public class OccludedWithinAreaChecking extends EffectiveCoverageChecking {

	public OccludedWithinAreaChecking(
		Component spaceEntity,
		Collection<Component> effectSources,
		double effectRange,
		double minimumCoverage) {
		super(spaceEntity, effectSources, effectRange, minimumCoverage);
	}

	@Override
	MArea calculateCoverage(MArea sourceFootprint) {
		// calculate the visibility polygon for the source footprint
		VisibilityPolygonCalculator calc = VisibilityPolygonCalculator.fromAreaAndSource(spaceArea, sourceFootprint);
		MArea visibilityPolygon = MArea.create(calc.getVisibilityPolygonForEffectSource());

		// apply effect range
		MArea sourceCopy = MArea.create(sourceFootprint);
		sourceCopy.resize(effectRange, true);
		visibilityPolygon.intersect(sourceCopy);
		return visibilityPolygon;
	}

}
