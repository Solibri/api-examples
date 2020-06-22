package com.solibri.smc.api.view.examples;

import com.solibri.smc.api.settings.PasswordSetting;

public class ApiKeySetting extends PasswordSetting {

	@Override
	public String getUniqueId() {
		return "Google Search API Key";
	}

	@Override
	public String getDefaultValue() {
		return "abc";
	}

}
