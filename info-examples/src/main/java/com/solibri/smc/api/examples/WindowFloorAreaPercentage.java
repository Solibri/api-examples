package com.solibri.smc.api.examples;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.model.components.Space;

/**
 * Calculates the percentage of window area with regards to floor area for spaces.
 *
 * Uses space boundaries, but if space boundary is not defined for the floor area, defaults to calculating the area from
 * geometry.
 *
 */
public class WindowFloorAreaPercentage implements Information<Double> {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getUniqueId() {
		return "WindowFloorAreaPercentage";
	}

	@Override
	public String getName() {
		return "Percentage of window area against floor area";
	}

	@Override
	public Optional<Double> getInformation(Component component) {
		if (!(component instanceof Space)) {
			return Optional.empty();
		}
		Space space = (Space) component;
		double windowArea = space.getSpaceBoundariesWindowsArea();
		double floorArea = space.getSpaceBoundariesFloorsArea();
		if (floorArea == 0.0) {
			LOG.warn("Space {} had zero floor area.", space.getName());
			floorArea = space.getFootprint().getArea().getSize();
		}
		return Optional.of(windowArea / floorArea);
	}

	@Override
	public PropertyType getType() {
		return PropertyType.PERCENTAGE;
	}

}
