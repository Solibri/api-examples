package com.solibri.smc.api.examples.beginner;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIComponent;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerHorizontal;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UIImage;
import com.solibri.smc.api.ui.UILabel;
import com.solibri.smc.api.ui.UIRuleParameter;

/**
 * Class that provides the UI layout for the Heatmap Visualization Rule.
 */
class HeatmapVisualizationRuleUIDefinition {

	/**
	 * The heatmap visualization rule.
	 */
	private final HeatmapVisualizationRule heatmapVisualizationRule;

	/**
	 * The UI definition container.
	 */
	private final UIContainer uiDefinition;

	/**
	 * The resources of the rule.
	 */
	private final RuleResources resources;

	/**
	 * Constructor.
	 *
	 * @param heatmapVisualizationRule the heatmap visualization rule
	 */
	public HeatmapVisualizationRuleUIDefinition(HeatmapVisualizationRule heatmapVisualizationRule) {
		this.heatmapVisualizationRule = heatmapVisualizationRule;
		this.resources = RuleResources.of(heatmapVisualizationRule);
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
		 * Create the vertical component container.
		 */
		UIContainer uiContainer = UIContainerVertical.create(
			resources.getString("UI.HeatmapVisualizationRule.TITLE"),
			BorderType.LINE);

		/*
		 * Add the description.
		 */
		uiContainer
			.addComponent(UILabel.create(resources.getString("UI.HeatmapVisualizationRule.DESCRIPTION")));

		/*
		 * Add the first filter for components to check.
		 */
		uiContainer.addComponent(createComponentFilterUIDefinition());

		/*
		 * Add second filter for source effect components.
		 */
		uiContainer.addComponent(createEffectSourceComponentFilterUIDefinition());

		/*
		 * Add visualization parameters content.
		 */
		uiContainer.addComponent(createParameterValuesUIDefinition());

		return uiContainer;
	}

	private UIComponent createComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();

		/*
		 * Add component filter.
		 */
		uiContainer.addComponent(UIRuleParameter.create(heatmapVisualizationRule.rpComponentFilter));

		return uiContainer;
	}

	private UIComponent createEffectSourceComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();

		uiContainer.addComponent(UIRuleParameter.create(heatmapVisualizationRule.rpEffectSourceFilter));

		return uiContainer;
	}

	private UIComponent createParameterValuesUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create(
			resources.getString("UI.HeatmapVisualizationParameter.TITLE"),
			BorderType.LINE);

		uiContainer
			.addComponent(UILabel.create(resources.getString("UI.HeatmapVisualizationParameter.DESCRIPTION")));

		/*
		 * Add the horizontal container with image and parameters.
		 */
		UIContainer horizontalUiContainer = UIContainerHorizontal.create();
		UIImage image = UIImage.create(resources.getImageUrl("heatmap_visualization_rule_parameters.png"));
		horizontalUiContainer.addComponent(image);
		horizontalUiContainer.addComponent(createParameterUIDefinition());
		uiContainer.addComponent(horizontalUiContainer);

		return uiContainer;
	}

	private UIContainer createParameterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();

		uiContainer.addComponent(UIRuleParameter.create(heatmapVisualizationRule.rpRangeParameter));
		uiContainer.addComponent(UIRuleParameter.create(heatmapVisualizationRule.rpStepParameter));

		return uiContainer;
	}
}
