package com.solibri.smc.api.examples;

import java.util.Collection;
import java.util.Optional;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.model.components.Door;
import com.solibri.smc.api.model.components.Space;

/**
 * This example custom Information fetches the height ratio between the largest and the smallest door the space has.
 *
 * The information is defined only for {@link Space}s.
 */
public class RatioOfDoors implements Information<Double> {

	@Override
	public String getUniqueId() {
		return "Size ratio of doors";
	}

	@Override
	public Optional<Double> getInformation(Component component) {
		// This is not defined for non-spaces.
		if (!(component instanceof Space)) {
			return Optional.empty();
		}
		Space space = (Space) component;
		Collection<Door> doors = space.getDoors();

		if (doors.isEmpty()) {
			return Optional.empty();
		}
		/*
		 * If only one door, the ratio is defined to be 1.0.
		 */
		if (doors.size() == 1) {
			return Optional.of(1.0);
		}
		double maxHeight = -Double.MAX_VALUE;
		double minHeight = Double.MAX_VALUE;
		for (Door door : doors) {
			double sizeZ = door.getBoundingBox().getSizeZ();
			if (sizeZ != 0.0 && sizeZ > maxHeight) {
				maxHeight = sizeZ;
			}
			if (sizeZ != 0.0 && sizeZ < minHeight) {
				minHeight = sizeZ;
			}
		}

		return Optional.of(maxHeight / minHeight);
	}

	@Override
	public PropertyType getType() {
		return PropertyType.DOUBLE;
	}

}
