package com.solibri.smc.api.view.examples.spaceinformation;

import java.io.File;
import java.util.Optional;

import com.solibri.smc.api.settings.StringSetting;

public class ExportToExcelFileLocationSetting extends StringSetting {

	@Override
	public String getUniqueId() {
		return "Space information Excel export file";
	}

	@Override
	public String getDefaultValue() {
		return System.getProperty("user.home") + "/space-information.xlsx";
	}

	@Override
	public Optional<String> invalidReason(String value) {
		File file = new File(value);
		if (file.getPath().endsWith(".xlsx")) {
			return Optional.empty();
		}
		return Optional.of("The file has to end in .xlsx.");
	}

}
