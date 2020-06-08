package com.solibri.smc.api.examples;

import java.util.Arrays;
import java.util.List;

import com.solibri.smc.api.settings.Setting;
import com.solibri.smc.api.settings.SettingGroup;

public class DynamicInfoSettingGroup implements SettingGroup {

	@Override
	public String getUniqueId() {
		return "Dynamic-information-settings";
	}

	@Override
	public List<Class<? extends Setting<?>>> getSettings() {
		return Arrays.asList(DynamicNameSetting.class, DynamicValueSetting.class);
	}

}
