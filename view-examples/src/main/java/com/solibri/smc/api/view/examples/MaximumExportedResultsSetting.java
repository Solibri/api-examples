package com.solibri.smc.api.view.examples;

import java.util.Optional;

import com.solibri.smc.api.settings.IntegerSetting;

public class MaximumExportedResultsSetting extends IntegerSetting {

	@Override
	public String getUniqueId() {
		return "Maximum exported checking results";
	}

	@Override
	public Integer getDefaultValue() {
		return 1024;
	}

	@Override
	public Optional<String> invalidReason(Integer value) {
		// We don't allow the value to be negative or zero.
		if (value <= 0) {
			return Optional.of("The given value has to be positive.");
		}
		return Optional.empty();

	}

}
