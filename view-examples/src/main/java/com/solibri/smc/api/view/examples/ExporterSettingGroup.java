package com.solibri.smc.api.view.examples;

import java.util.ArrayList;
import java.util.List;

import com.solibri.smc.api.settings.Setting;
import com.solibri.smc.api.settings.SettingGroup;

public class ExporterSettingGroup implements SettingGroup {

	@Override
	public String getUniqueId() {
		return "exporter-setting-group";
	}

	@Override
	public String getName() {
		return "Checking results exporter";
	}

	@Override
	public List<Class<? extends Setting<?>>> getSettings() {
		List<Class<? extends Setting<?>>> list = new ArrayList<>();
		list.add(MaximumExportedResultsSetting.class);
		list.add(OverwriteOldExportsSetting.class);
		return list;
	}

}
