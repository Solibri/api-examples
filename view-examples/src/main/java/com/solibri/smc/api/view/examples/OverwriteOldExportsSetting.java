package com.solibri.smc.api.view.examples;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import com.solibri.smc.api.settings.BooleanSetting;

public class OverwriteOldExportsSetting extends BooleanSetting {

	@Override
	public String getUniqueId() {
		return "Overwrite old exports";
	}

	@Override
	public Boolean getDefaultValue() {
		return Boolean.FALSE;
	}

	@Override
	public Optional<String> invalidReason(Boolean value) {
		/*
		 * If the current minute is even, any value can be chosen, otherwise overwriting old exporting is not allowed to
		 * be chosen.
		 */
		if (value && LocalDateTime.now(ZoneId.systemDefault()).getMinute() % 2 != 0) {
			return Optional.of("You cannot overwrite results right now.");
		}
		return Optional.empty();

	}

}
