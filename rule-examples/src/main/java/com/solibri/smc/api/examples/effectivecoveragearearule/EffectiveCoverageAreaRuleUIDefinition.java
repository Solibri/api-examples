package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.util.ArrayList;
import java.util.List;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerHorizontal;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UIImage;
import com.solibri.smc.api.ui.UIRadioButtonPanel;
import com.solibri.smc.api.ui.UIRadioButtonPanelHorizontal;
import com.solibri.smc.api.ui.UIRuleParameter;

/**
 * This class provides UI to check the Effective coverage area rule.
 */
final class EffectiveCoverageAreaRuleUIDefinition {

	private final EffectiveCoverageAreaRule rule;
	private final UIContainer uiDefinition;
	private final RuleResources resources;

	EffectiveCoverageAreaRuleUIDefinition(EffectiveCoverageAreaRule rule) {
		this.rule = rule;
		this.resources = RuleResources.of(rule);
		this.uiDefinition = createUIDefinition();
	}

	UIContainer getDefinitionContainer() {
		return uiDefinition;
	}

	private UIContainer createUIDefinition() {
		UIContainer uiDefinitionContainer = UIContainerVertical.create();

		uiDefinitionContainer.addComponent(createSpacesToCheckFilterPanel());
		uiDefinitionContainer.addComponent(createEffectSourcesFilterPanel());
		uiDefinitionContainer.addComponent(createEffectParametersPanel());
		uiDefinitionContainer.addComponent(createCoverageParametersPanel());
		uiDefinitionContainer.addComponent(createRatioCheckParametersPanel());

		return uiDefinitionContainer;
	}

	private UIContainer createSpacesToCheckFilterPanel() {
		UIContainer spacesToCheckPanel = UIContainerVertical
			.create(resources.getString("uiComponentFilterPanel.TITLE"), BorderType.LINE);
		spacesToCheckPanel.addComponent(UIRuleParameter.create(rule.rpSpacesToCheck));
		return spacesToCheckPanel;
	}

	private UIContainer createEffectSourcesFilterPanel() {
		UIContainer effectSourcesPanel = UIContainerVertical
			.create(resources.getString("uiEffectSourcesPanel.TITLE"), BorderType.LINE);
		effectSourcesPanel.addComponent(UIRuleParameter.create(rule.rpEffectSources));
		return effectSourcesPanel;
	}

	private UIContainer createEffectParametersPanel() {
		UIContainer effectParametersPanel = UIContainerVertical
			.create(resources.getString("uiEffectParametersPanel.TITLE"), BorderType.LINE);
		UIContainer parametersPanelWithRightPadding = UIContainerHorizontal.create();

		UIContainer parametersPane = UIContainerVertical.create();
		parametersPane.addComponent(UIRuleParameter.create(rule.rpEffectRange));
		parametersPane.addComponent(UIRuleParameter.create(rule.rpPropagateToConnectedSpaces));

		parametersPanelWithRightPadding.addComponent(parametersPane);
		// Add padding to the right the force parameters to the left
		parametersPanelWithRightPadding.addComponent(UIContainerVertical.create());
		effectParametersPanel.addComponent(parametersPanelWithRightPadding);

		UIContainer behaviorSelectionPanel = UIContainerVertical
			.create(resources.getString("rpOcclusionAndBounds.NAME"), BorderType.LINE);

		UIRadioButtonPanel behaviorRadiobuttons = UIRadioButtonPanelHorizontal.create(rule.rpOcclusionAndBounds);
		behaviorRadiobuttons.addOptionImages(getOptionImages());
		behaviorSelectionPanel.addComponent(behaviorRadiobuttons);

		effectParametersPanel.addComponent(behaviorSelectionPanel);

		return effectParametersPanel;
	}

	private List<UIImage> getOptionImages() {
		List<UIImage> imageLabelRow = new ArrayList<>();

		// These are the images for the different radio button options.
		imageLabelRow.add(
			UIImage.create(resources.getImageUrl(resources.getString("rpOcclusionAndBounds.Unoccluded.IMAGE_NAME"))));
		imageLabelRow.add(UIImage.create(
			resources.getImageUrl(resources.getString("rpOcclusionAndBounds.UnoccludedWithinArea.IMAGE_NAME"))));
		imageLabelRow.add(UIImage
			.create(resources.getImageUrl(resources.getString("rpOcclusionAndBounds.DistanceOfTravel.IMAGE_NAME"))));
		imageLabelRow.add(UIImage
			.create(resources.getImageUrl(resources.getString("rpOcclusionAndBounds.OccludedWithinArea.IMAGE_NAME"))));

		return imageLabelRow;
	}

	private UIContainer createCoverageParametersPanel() {
		UIContainer effectiveCoveragePanel = UIContainerVertical
			.create(resources.getString("uiEffectCoveragePanel.TITLE"), BorderType.LINE);
		effectiveCoveragePanel.addComponent(UIRuleParameter.create(rule.rpMinimumCoverage));
		return effectiveCoveragePanel;
	}

	private UIContainer createRatioCheckParametersPanel() {
		UIContainer propertyValuesPanel = UIContainerVertical
			.create(resources.getString("uiRequiredRatioPanel.TITLE"), BorderType.LINE);

		UIContainer parameterPane = UIContainerVertical.create();
		parameterPane.addComponent(UIRuleParameter.create(rule.rpRequiredMinimumRatio));
		propertyValuesPanel.addComponent(parameterPane);

		UIContainer propertySelectorPanel = UIContainerVertical
			.create(resources.getString("uiPropertyValuesPanel.TITLE"), BorderType.LINE);
		propertySelectorPanel.addComponent(UIRuleParameter.create(rule.rpEffectSourcePropertyReference));
		propertySelectorPanel.addComponent(UIRuleParameter.create(rule.rpEffectSourceMultiplier));
		propertySelectorPanel.addComponent(UIRuleParameter.create(rule.rpAreaPropertyReference));

		propertyValuesPanel.addComponent(propertySelectorPanel);

		return propertyValuesPanel;
	}
}
