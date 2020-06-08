package com.solibri.info;

import java.util.Optional;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;

/**
 * This example custom Information fetches the middle elevation of a component calculated by taking the average from
 * global top elevation and global bottom elevation.
 */
public class ExampleInfo implements Information<Double> {

	@Override
	public String getUniqueId() {
		return "ExampleInfo-get-middle-elevation";
	}

	@Override
	public Optional<Double> getInformation(Component component) {
		return Optional.of(Double.valueOf((component.getGlobalTopElevation() + component.getGlobalBottomElevation()) / 2));
	}

	@Override
	public PropertyType getType() {
		return PropertyType.LENGTH;
	}

}
