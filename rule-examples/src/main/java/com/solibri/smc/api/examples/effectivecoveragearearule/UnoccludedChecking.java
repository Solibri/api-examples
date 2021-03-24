package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.Collection;

import com.solibri.geometry.primitive2d.MArea;
import com.solibri.smc.api.model.Component;

class UnoccludedChecking extends EffectiveCoverageChecking {

	public UnoccludedChecking(
		Component spaceEntity,
		Collection<Component> effectSources,
		double effectRange,
		double minimumCoverage) {
		super(spaceEntity, effectSources, effectRange, minimumCoverage);
	}

	@Override
	MArea calculateCoverage(MArea sourceFootprint) {
		MArea footprintCopy = MArea.create(sourceFootprint);
		footprintCopy.resize(effectRange, true);
		return footprintCopy;
	}
}
