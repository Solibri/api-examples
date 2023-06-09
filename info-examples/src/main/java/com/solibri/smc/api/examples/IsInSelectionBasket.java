package com.solibri.smc.api.examples;

import java.util.Optional;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;

/**
 * This example custom Information tells if the given component is currently in the selection basket.
 */
public class IsInSelectionBasket implements Information<Boolean> {

	@Override
	public String getUniqueId() {
		return "Is in selection basket";
	}

	@Override
	public Optional<Boolean> getInformation(Component component) {
		return Optional.of(SMC.getSelectionBasket().get().contains(component));
	}

	@Override
	public PropertyType getType() {
		return PropertyType.BOOLEAN;
	}

}
