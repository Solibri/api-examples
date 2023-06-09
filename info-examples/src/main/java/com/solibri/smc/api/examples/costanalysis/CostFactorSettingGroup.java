package com.solibri.smc.api.examples.costanalysis;

import java.util.ArrayList;
import java.util.List;

import com.solibri.smc.api.settings.Setting;
import com.solibri.smc.api.settings.SettingGroup;

/**
 * This setting groups creates a single groupin the UI for cost factor settings.
 */
public class CostFactorSettingGroup implements SettingGroup {

	@Override
	public String getUniqueId() {
		return "cost-factor-setting-group";
	}

	@Override
	public String getName() {
		return "Cost factor settings";
	}

	@Override
	public List<Class<? extends Setting<?>>> getSettings() {
		List<Class<? extends Setting<?>>> list = new ArrayList<>();
		list.add(WallMaterialCostClassificationNameSetting.class);
		return list;
	}

}
