package com.solibri.smc.api.examples;

import java.util.Optional;

import com.solibri.smc.api.info.InformationParameters;
import com.solibri.smc.api.info.ParametricInformation;
import com.solibri.smc.api.info.StringInformationParameter;
import com.solibri.smc.api.model.Component;

/**
 * This example custom ParametricInformation fetches whether or not a component has specific propertyset and property.
 *
 * @see ParametricInformation
 * @since 9.12.7
 */
public class HasPropertySetAndProperty implements ParametricInformation<Boolean> {

	/**
	 * Retrieve the parameter creation handler, used to define parameters for this parametic information.
	 */
	private final InformationParameters params = InformationParameters.of(this);

	private final StringInformationParameter propertySetName = params.createString("Property Set Name", "");

	private final StringInformationParameter propertyName = params.createString("Property Name", "");

	@Override
	public String getUniqueId() {
		return "Property Set And Property";
	}

	@Override
	public Optional<Boolean> getInformation(Component component) {
		String propertySetNameValue = propertySetName.getValue();
		String propertyNameValue = propertyName.getValue();
		boolean hasProperty = component.getPropertySets(propertySetNameValue).stream()
			.flatMap(pset -> pset.getProperty(propertyNameValue).stream())
			.findAny().isPresent();
		return Optional.of(hasProperty);
	}

}
