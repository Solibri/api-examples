package com.solibri.smc.api.examples.spaceconnectionrule;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerHorizontal;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UIRadioButtonPanelVertical;
import com.solibri.smc.api.ui.UIRuleParameter;

/**
 * Class that provides the UI layout for the Space Connection rule.
 */
class SpaceConnectionRuleUIDefinition {

	private final SpaceConnectionRule spaceConnectionRule;
	private final UIContainer uiDefinition;

	public SpaceConnectionRuleUIDefinition(SpaceConnectionRule spaceConnectionRule) {
		this.spaceConnectionRule = spaceConnectionRule;
		this.uiDefinition = createUIDefinition();
	}

	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}

	private UIContainer createUIDefinition() {

		RuleResources resources = RuleResources.of(spaceConnectionRule);

		UIContainer rootContainer = UIContainerVertical.create("", BorderType.LINE);

		UIContainer outsideContainer = UIContainerVertical
			.create(resources.getString("UI.SpacesToCheck.TITLE"), BorderType.LINE);
		UIContainer spaceToSpaceContainer = UIContainerVertical
			.create(resources.getString("UI.SpacesToCheck.TITLE"), BorderType.LINE);

		UIContainer checkBoxContainer1 = UIContainerVertical
			.create(resources.getString("UI.SpaceConnectionRequirements.TITLE"),
				BorderType.LINE);

		UIContainer checkBoxContainer1_1 = UIContainerHorizontal
			.create(resources.getString("rpDirectAccessOutsideCondition.TITLE"),
				BorderType.LINE);
		checkBoxContainer1_1
			.addComponent(UIRadioButtonPanelVertical.create(spaceConnectionRule.rpDirectAccessOutsideCondition));
		checkBoxContainer1.addComponent(checkBoxContainer1_1);

		UIContainer checkBoxContainer1_2 = UIContainerHorizontal
			.create(resources.getString("rpTypeOfAccessOutsideCondition.TITLE"),
				BorderType.LINE);
		checkBoxContainer1_2
			.addComponent(UIRadioButtonPanelVertical.create(spaceConnectionRule.rpTypeOfAccessOutsideCondition));
		checkBoxContainer1.addComponent(checkBoxContainer1_2);

		UIContainer checkBoxContainer2 = UIContainerVertical
			.create(resources.getString("UI.SpaceConnectionRequirements.TITLE"),
				BorderType.LINE);
		UIContainer checkBoxContainer2_1 = UIContainerHorizontal
			.create(resources.getString("rpDirectAccessCondition.TITLE"),
				BorderType.LINE);
		checkBoxContainer2_1
			.addComponent(UIRadioButtonPanelVertical.create(spaceConnectionRule.rpDirectAccessCondition));
		checkBoxContainer2.addComponent(checkBoxContainer2_1);

		UIContainer checkBoxContainer2_2 = UIContainerHorizontal
			.create(resources.getString("rpTypeOfAccessCondition.TITLE"),
				BorderType.LINE);
		checkBoxContainer2_2
			.addComponent(UIRadioButtonPanelVertical.create(spaceConnectionRule.rpTypeOfAccessCondition));
		checkBoxContainer2.addComponent(checkBoxContainer2_2);

		outsideContainer.addComponent(UIRuleParameter.create(spaceConnectionRule.rpSpacesFilterA));
		outsideContainer.addComponent(checkBoxContainer1);
		spaceToSpaceContainer.addComponent(UIRuleParameter.create(spaceConnectionRule.rpSpacesFilterB));
		spaceToSpaceContainer.addComponent(checkBoxContainer2);

		rootContainer.addComponent(outsideContainer);
		rootContainer.addComponent(spaceToSpaceContainer);

		return rootContainer;
	}

}
