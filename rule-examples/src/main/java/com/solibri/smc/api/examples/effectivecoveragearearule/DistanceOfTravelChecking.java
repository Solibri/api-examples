package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.Collection;

import com.solibri.geometry.primitive2d.MArea;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.examples.effectivecoveragearearule.visibility.DistanceOfTravelEffectCalculator;

public class DistanceOfTravelChecking extends EffectiveCoverageChecking {

	public DistanceOfTravelChecking(
		Component spaceEntity,
		Collection<Component> effectSources,
		double effectRange,
		double minimumCoverage) {
		super(spaceEntity, effectSources, effectRange, minimumCoverage);
	}

	@Override
	MArea calculateCoverage(MArea sourceFootprint) {
		DistanceOfTravelEffectCalculator calculator = DistanceOfTravelEffectCalculator
			.fromAreaAndSourceAndEffectRange(spaceArea, sourceFootprint, effectRange);
		MArea effectArea = MArea.create(calculator.getEffectArea());
		effectArea.add(calculator.getGeometryGraph().getPossiblyResizedSource());
		effectArea.intersect(spaceArea);
		return effectArea;
	}

}
