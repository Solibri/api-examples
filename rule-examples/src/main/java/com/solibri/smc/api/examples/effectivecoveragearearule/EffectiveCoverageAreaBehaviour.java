package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.Arrays;

public enum EffectiveCoverageAreaBehaviour {
	/* Sources outside the space will contribute to coverage */
	UNOCCLUDED("rpOcclusionAndBounds.Unoccluded"),

	/* Sources outside the space will not contribute to coverage */
	UNOCCLUDED_WITHIN_AREA("rpOcclusionAndBounds.UnoccludedWithinArea"),

	/* The effect will bend around corners */
	DISTANCE_OF_TRAVEL_WITHIN_AREA("rpOcclusionAndBounds.DistanceOfTravel"),

	/* An effect source will cover the area visible from the source */
	OCCLUDED_WITHIN_AREA("rpOcclusionAndBounds.OccludedWithinArea");

	private final String propertyKey;

	static EffectiveCoverageAreaBehaviour fromPropertyKey(String propertyKey) {
		return Arrays.stream(EffectiveCoverageAreaBehaviour.values())
			.filter(behavior -> behavior.getPropertyKey().equals(propertyKey))
			.findFirst()
			.get();
	}

	private EffectiveCoverageAreaBehaviour(String propertyKey) {
		this.propertyKey = propertyKey;
	}

	String getPropertyKey() {
		return propertyKey;
	}
}
