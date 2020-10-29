package com.solibri.smc.api.examples;

import java.util.Collection;
import java.util.Optional;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;

/**
 * This example custom Information tells if the given component relates to any of the results from the latest checking
 * run.
 */
public class HasFailedChecking implements Information<Boolean> {

	@Override
	public String getUniqueId() {
		return "Has failed checking";
	}

	@Override
	public Optional<Boolean> getInformation(Component component) {
		Collection<Result> results = SMC.getChecking().getResults();
		return Optional.of(results.stream().anyMatch(r -> r.getInvolvedComponents().contains(component)));
	}

	@Override
	public PropertyType getType() {
		return PropertyType.BOOLEAN;
	}

}
