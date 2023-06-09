package com.solibri.smc.api.examples;

import java.util.Optional;

import com.solibri.smc.api.settings.StringSetting;

public class DynamicValueSetting extends StringSetting {

	@Override
	public String getUniqueId() {
		return " ";
	}

	@Override
	public String getDefaultValue() {
		return "private java.lang.String get(com.solibri.smc.api.model.Component component) {\n"
			+ "		return component.getGUID();\n"
			+ "	}";
	}

	@Override
	public Optional<String> invalidReason(String value) {
		return Optional.empty();
	}

	@Override
	public boolean isMultiLine() {
		return true;
	}

}
