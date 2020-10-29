package com.solibri.smc.api.view.examples;

import java.util.ArrayList;
import java.util.List;

import com.solibri.smc.api.settings.SettingDialog;
import com.solibri.smc.api.settings.SettingGroup;

public class ExporterSettingDialog implements SettingDialog {

	@Override
	public String getUniqueId() {
		return "exporter-setting-dialog";
	}

	@Override
	public String getName() {
		return "Checking results exporter";
	}

	@Override
	public List<Class<? extends SettingGroup>> getSettingGroups() {
		List<Class<? extends SettingGroup>> list = new ArrayList<>();
		list.add(ExporterSettingGroup.class);
		return list;
	}

}
