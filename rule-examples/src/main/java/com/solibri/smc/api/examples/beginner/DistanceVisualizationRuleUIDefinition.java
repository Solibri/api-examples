package com.solibri.smc.api.examples.beginner;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIComponent;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UILabel;
import com.solibri.smc.api.ui.UIRuleParameter;

/**
 * Class that provides the UI layout for the Distance Visualization Rule.
 * The UI consists of two component filters and one double valued parameter field for length.
 */
class DistanceVisualizationRuleUIDefinition {

	/**
	 * The distance visualization rule.
	 */
	private final DistanceVisualizationRule distanceVisualizationRule;

	/**
	 * The UI definition container.
	 */
	private final UIContainer uiDefinition;

	/**
	 * Basic constructor.
	 *
	 * @param distanceVisualizationRule the distance visualization rule
	 */
	public DistanceVisualizationRuleUIDefinition(DistanceVisualizationRule distanceVisualizationRule) {
		this.distanceVisualizationRule = distanceVisualizationRule;
		this.uiDefinition = createUIDefinition();
	}

	/**
	 * Returns the UI definition of the Rule.
	 *
	 * @return the UI definition container of the Rule
	 */
	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}

	private UIContainer createUIDefinition() {
		/*
		 * Fetch the resources for this rule.
		 */
		RuleResources resources = RuleResources.of(distanceVisualizationRule);

		/*
		 * Create the vertical component container.
		 */
		UIContainer uiContainer = UIContainerVertical.create(
			resources.getString("UI.DistanceVisualizationRule.TITLE"),
			BorderType.LINE);

		/*
		 * Add the description.
		 */
		uiContainer
			.addComponent(UILabel.create(resources.getString("UI.DistanceVisualizationRule.DESCRIPTION")));

		/*
		 * Add the first filter for components to check.
		 */
		uiContainer.addComponent(createComponentFilterUIDefinition());

		/*
		 * Add second filter for components to check.
		 */
		uiContainer.addComponent(createSecondComponentFilterUIDefinition());

		/*
		 * Add third filter for the range of the secondary components.
		 */
		uiContainer.addComponent(createMaximumDistanceFilterUIDefinition());

		return uiContainer;
	}

	private UIComponent createComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(distanceVisualizationRule.rpComponentFilter));

		return uiContainer;
	}

	private UIComponent createSecondComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(distanceVisualizationRule.rpComponentFilter2));

		return uiContainer;
	}

	private UIComponent createMaximumDistanceFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(distanceVisualizationRule.maximumDistance));

		return uiContainer;
	}
}
