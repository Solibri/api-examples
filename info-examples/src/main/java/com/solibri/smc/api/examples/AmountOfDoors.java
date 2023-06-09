package com.solibri.smc.api.examples;

import java.util.Collection;
import java.util.Optional;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.model.components.Door;
import com.solibri.smc.api.model.components.Space;

/**
 * This example custom Information fetches the amount of doors that a space has.
 *
 * The information is defined only for {@link Space}s.
 */
public class AmountOfDoors implements Information<Integer> {

	@Override
	public String getUniqueId() {
		return "Amount of doors in a space";
	}

	@Override
	public Optional<Integer> getInformation(Component component) {
		// This is not defined for non-spaces.
		if (!(component instanceof Space)) {
			return Optional.empty();
		}
		Space space = (Space) component;
		Collection<Door> doors = space.getDoors();

		return Optional.of(doors.size());
	}

	@Override
	public PropertyType getType() {
		return PropertyType.INT;
	}

}
