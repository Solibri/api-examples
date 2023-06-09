package com.solibri.smc.api.examples.beginner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.intersection.Intersection;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.ui.UIContainer;

/**
 * An example rule that checks all clashes between components. A tolerance
 * parameter is included for specifying how much volume the clash must have to
 * be considered an issue.
 */
public final class ClashDetectionRule extends OneByOneRule {

	/**
	 * Constant strings are created to be used when creating different rule
	 * parameters. The declaration of constants keeps the code clean and
	 * reusable.
	 */
	private static final String COMPONENT_FILTER_PARAMETER_ID2 = "rpComponentFilter2";

	private static final String ALLOWED_TOLERANCE_PARAMETER_ID = "rpAllowedTolerance";

	/**
	 * Retrieve the rule parameters handler, used to define parameters for
	 * this rule.
	 */
	private final RuleParameters params = RuleParameters.of(this);

	/**
	 * The default FilterParameter is used for source components, and a new one is
	 * created for target components. A DoubleParameter is created to state the
	 * allowed tolerance for components.
	 * Every component that passes the default filter filter is then forwarded to
	 * the check method.
	 */
	final FilterParameter rpComponentFilter = this.getDefaultFilterParameter();

	final FilterParameter rpComponentFilter2 = params.createFilter(COMPONENT_FILTER_PARAMETER_ID2);

	final DoubleParameter rpAllowedTolerance = params.createDouble(ALLOWED_TOLERANCE_PARAMETER_ID, PropertyType.VOLUME);

	/**
	 * Add the UI definition from ClashDetectionRuleUIDefinition class.
	 */
	private final ClashDetectionRuleUIDefinition uiDefinition = new ClashDetectionRuleUIDefinition(this);

	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		/*
		 * Get the values from the model using a geometric filter combined with the
		 * second filter from the UI. It is best to start the filter chains with
		 * a geometric filter to allow the use of optimized geometric queries.
		 */
		ComponentFilter secondFilter = rpComponentFilter2.getValue();
		ComponentFilter targetComponentFilter = AABBIntersectionFilter.ofComponentBounds(component).and(secondFilter);
		Collection<Component> targets = SMC.getModel().getComponents(targetComponentFilter);

		/*
		 * Run the clash check for each component in the bounding box.
		 */
		Collection<Result> results = new ArrayList<>();
		for (Component target : targets) {
			results.addAll(clashCheck(component, target, resultFactory));
		}

		return results;
	}

	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}

	/**
	 * Checks for clashes between components and generates results.
	 *
	 * @param source the source component
	 * @param target the target component
	 */
	private Collection<Result> clashCheck(Component source, Component target, ResultFactory resultFactory) {

		final double allowedTolerance = rpAllowedTolerance.getValue();
		final double transparency = 0.5;

		Collection<Result> results = new ArrayList<>();

		Set<Intersection> intersections = source.getIntersections(target);

		/*
		 * Loop through each intersection to find the involved components, create
		 * results, and visualize them.
		 */
		for (Intersection intersection : intersections) {
			if (intersection.getVolume() < allowedTolerance) {
				continue;
			} else {
				String name = source.getName() + " clashes with " + target.getName();
				String description = "There is a clash between " + source.getName() + " and " + target.getName();

				Result result = resultFactory
					.create(name, description)
					.withInvolvedComponent(target)
					.withVisualization(visualization -> {
						visualization.addComponent(source, transparency);
						visualization.addComponent(target, transparency);
					});

				results.add(result);
			}
		}

		return results;
	}
}
