package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.Collection;

import com.solibri.geometry.primitive2d.Area;
import com.solibri.smc.api.model.Component;

class CoverageAreaViolation {

	final Component spaceEntity;
	final Collection<Component> effectSources;
	final Area coverage;
	final double coverageRatio;
	final double minimumCoverage;

	public CoverageAreaViolation(
		Component spaceEntity,
		Collection<Component> effectSources,
		Area coverage,
		double coverageArea,
		double coverageRatio,
		double minimumCoverage) {
		this.spaceEntity = spaceEntity;
		this.effectSources = effectSources;
		this.coverage = coverage;
		this.coverageRatio = coverageRatio;
		this.minimumCoverage = minimumCoverage;
	}
}
