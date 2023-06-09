package com.solibri.smc.api.examples;

import java.io.File;
import java.util.Optional;

import com.solibri.smc.api.settings.StringSetting;

public class ExcelFileLocationSetting extends StringSetting {

	@Override
	public String getUniqueId() {
		return "Excel file location";
	}

	@Override
	public String getDefaultValue() {
		return System.getProperty("user.home") + "/ids.xlsx";
	}

	@Override
	public Optional<String> invalidReason(String value) {
		File file = new File(value);
		if (file.exists() && file.getPath().endsWith(".xlsx")) {
			return Optional.empty();
		}
		return Optional.of("The file has to exist and be an Excel file.");
	}

}
