package com.solibri.smc.api.examples.costanalysis;

import java.util.Optional;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.model.Quantities;
import com.solibri.smc.api.model.Quantities.Type;
import com.solibri.smc.api.model.components.Wall;

/**
 * WallMaterialCostInformation calculates the cost for {@link Wall}s by multiplying the volume of the wall with the cost
 * factor found from chosen classification.
 *
 * The cost factor is intended to contain information for how much the given wall costs per volume unit.
 *
 * The custom information assumes that all Walls are classified and that all of them contain a proper classification
 * value in a format that can be parsed into a double. No error handling for other cases has been implemented.
 */
public class WallMaterialCostInformation implements Information<Double> {

	@Override
	public String getUniqueId() {
		return "Cost of wall materials";
	}

	@Override
	public Optional<Double> getInformation(Component component) {
		if (!(component instanceof Wall)) {
			return Optional.empty();
		}
		Wall wall = (Wall) component;
		// total cost = volume * cost factor
		Double volume = Quantities.of(wall).get(Type.VOLUME).get();
		WallMaterialCostClassificationNameSetting setting =
				SMC.getSettings().getSetting(WallMaterialCostClassificationNameSetting.class);
		String resultName = SMC.getModel()
				.getClassification(setting.getValue())
				.get().classify(wall).iterator().next()
		.getName();
		double costFactor = Double.parseDouble(resultName);

		return Optional.of(volume * costFactor);
	}

	@Override
	public PropertyType getType() {
		return PropertyType.DOUBLE;
	}

}
