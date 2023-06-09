package com.solibri.smc.api.examples.costanalysis;

import java.util.ArrayList;
import java.util.List;

import com.solibri.smc.api.settings.SettingDialog;
import com.solibri.smc.api.settings.SettingGroup;

/**
 * This setting dialog adds a separate setting dialog for all cost analysis related settings.
 */
public class CostAnalysisSettingDialog implements SettingDialog {

	@Override
	public String getUniqueId() {
		return "cost-analysis-setting-dialog";
	}

	@Override
	public String getName() {
		return "Cost Analysis";
	}

	@Override
	public List<Class<? extends SettingGroup>> getSettingGroups() {
		List<Class<? extends SettingGroup>> list = new ArrayList<>();
		list.add(CostFactorSettingGroup.class);
		return list;
	}

}
