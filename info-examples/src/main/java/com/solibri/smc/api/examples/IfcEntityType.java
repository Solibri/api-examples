package com.solibri.smc.api.examples;

import java.util.Optional;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;

/**
 * This custom information example fetches the IFC defined entity type for given
 * component.
 *
 * The information is defined only for {@link Component}s that have IFC defined
 * entity type.
 */
public class IfcEntityType implements Information<String> {

	@Override
	public String getUniqueId() {
		return "IFC entity type";
	}

	@Override
	public Optional<String> getInformation(Component component) {
		Optional<com.solibri.smc.api.ifc.IfcEntityType> ifcEntityType = component.getIfcEntityType();

		if (!ifcEntityType.isPresent()) {
			return Optional.empty();
		}

		return Optional.of(ifcEntityType.get().toString());
	}

}
