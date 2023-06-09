package com.solibri.smc.api.examples.beginner;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

import com.solibri.geometry.primitive3d.AABB3d;
import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.StringParameter;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;

/**
 * This example rule shows how to check the components' height. The height must
 * not exceed the limit set by the user. The OneByOneRule class provides functionality
 * for checking the components that pass the default filter one by one.
 */
public class ComponentHeightRule extends OneByOneRule {

	/**
	 * Retrieve the rule parameter creator for this rule.
	 */
	private final RuleParameters params = RuleParameters.of(this);

	/**
	 * A DoubleParameter allows the user to input a double value. The
	 * PropertyType is used to correctly format the double value using the units that are selected in the
	 * application settings. For example, when metres are used as the unit of length, then the value "5.0" will
	 * be formatted as "5.0 m" in the UI.
	 */
	final DoubleParameter maximumHeightDoubleParameter = params.createDouble("rpMaxHeight", PropertyType.LENGTH);

	/**
	 * A StringParameter allows the user to input text.
	 */
	final StringParameter resultNameStringParameter = params.createString("rpResultName");

	/**
	 * This method is called for every component that passes through the default filter.
	 *
	 * @param component the component that is checked by this method
	 * @param resultFactory the factory that is used for creating results for the checked component
	 * @return a collection of results associated with the component that is checked in this method
	 */
	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		/*
		 * This method returns the axis-aligned bounding box of the
		 * component.
		 */
		AABB3d componentBounds = component.getBoundingBox();

		/*
		 * The AABB object contains the bounds of the component in each dimension.
		 * The height of a component can be retrieved by getting the size of the bounding box
		 * along the Z-axis.
		 */
		double componentHeight = componentBounds.getSizeZ();

		/*
		 * The value that is set into a rule parameter can be accessed using the getValue method.
		 * This retrieves the maximum allowed height for components from the rule parameter.
		 */
		Double maximumAllowedHeight = maximumHeightDoubleParameter.getValue();

		/*
		 * Check if the component does not exceed the maximum height.
		 */
		if (componentHeight <= maximumAllowedHeight) {
			/*
			 * Return an empty collection of results because the component height does not
			 * exceed the maximum allowed height.
			 */
			return Collections.emptyList();
		}

		/*
		 * The component's height exceeds the allowed minimum, so a result is created for the
		 * component.
		 */

		/*
		 * The string parameter is used for getting the name of the result.
		 */
		String resultName = resultNameStringParameter.getValue();

		/*
		 * Create a formatted string of the component height. The format uses the
		 * units that are set in the application settings.
		 */
		String formattedComponentHeight = PropertyType.LENGTH.getFormat().format(componentHeight);

		/*
		 * Create a description of the result using the formatted component height.
		 */
		String resultDescription = MessageFormat
			.format("The height of component {0} exceeds the allowed maximum height {1}",
				component.getName(), formattedComponentHeight);

		/*
		 * Create a result with the name and description. In the check method of OneByOneRule
		 * the returned results are automatically associated with the component that is passed
		 * as a parameter.
		 */
		Result result = resultFactory.create(resultName, resultDescription);

		/*
		 * Only a single result is created for the component, so a singleton Set is
		 * created for the results.
		 */
		return Collections.singleton(result);
	}
}
