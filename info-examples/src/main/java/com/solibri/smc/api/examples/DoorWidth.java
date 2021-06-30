package com.solibri.smc.api.examples;

import java.util.Optional;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.model.components.Door;

/**
 * This custom information example fetches the overall width of the door.
 *
 * The information is defined only for {@link Door}s.
 */
public class DoorWidth implements Information<Double> {

	@Override
	public String getUniqueId() {
		return "Door overall width";
	}

	@Override
	public Optional<Double> getInformation(Component component) {

		if (!(component instanceof Door)) {
			return Optional.empty();
		}

		Door door = (Door) component;

		return Optional.of(door.getOverallWidth());
	}

	@Override
	public PropertyType getType() {
		return PropertyType.LENGTH;
	}

}
