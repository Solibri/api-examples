package com.solibri.smc.api.examples;

import java.util.Collection;
import java.util.Optional;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.model.components.Door;
import com.solibri.smc.api.model.components.Space;

/**
 * This example custom Information fetches whether or not a space has doors.
 *
 * The information is defined only for {@link Space}s.
 */
public class HasDoors implements Information<Boolean> {

	@Override
	public String getUniqueId() {
		return "Has doors";
	}

	@Override
	public Optional<Boolean> getInformation(Component component) {
		// This is not defined for non-spaces.
		if (!(component instanceof Space)) {
			return Optional.empty();
		}
		Space space = (Space) component;
		Collection<Door> doors = space.getDoors();
		return Optional.of(!doors.isEmpty());
	}

	@Override
	public PropertyType getType() {
		return PropertyType.BOOLEAN;
	}

}
