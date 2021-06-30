package com.solibri.smc.api.examples.costanalysis;

import com.solibri.smc.api.settings.StringSetting;

/**
 * This setting allows the user to define the used classification name in settings.
 */
public class WallMaterialCostClassificationNameSetting extends StringSetting {

	@Override
	public String getUniqueId() {
		return "Name of the cost factor classification";
	}

	@Override
	public String getDefaultValue() {
		return "Cost Per Cubic Meter";
	}

}
