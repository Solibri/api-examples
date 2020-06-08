package com.solibri.smc.api.examples;

import java.util.Optional;

import com.solibri.smc.api.settings.StringSetting;

public class DynamicNameSetting extends StringSetting {

	@Override
	public String getUniqueId() {
		return "Name for the dynamic information";
	}

	@Override
	public String getDefaultValue() {
		return "Dynamic information";
	}

	@Override
	public Optional<String> invalidReason(String value) {
		return Optional.empty();
	}

}
