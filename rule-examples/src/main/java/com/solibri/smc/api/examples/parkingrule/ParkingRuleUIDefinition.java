package com.solibri.smc.api.examples.parkingrule;

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
 * Class that provides the UI layout for the Parking rule.
 */
class ParkingRuleUIDefinition {

	private final ParkingRule parkingRule;
	private final UIContainer uiDefinition;
	private final RuleResources resources;

	public ParkingRuleUIDefinition(ParkingRule parkingRule) {
		this.parkingRule = parkingRule;
		this.resources = RuleResources.of(parkingRule);
		this.uiDefinition = createUIDefinition();
	}

	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}

	private UIContainer createUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();

		uiContainer.addComponent(createSpaceRequirementsUIDefinition());

		uiContainer.addComponent(createParkingSpaceAlignmentUIDefinition());

		uiContainer.addComponent(createObstructionCheckingUIDefinition());

		return uiContainer;
	}

	private UIComponent createSpaceRequirementsUIDefinition() {
		// Create the space requirements settings container
		UIContainer spaceRequirements = UIContainerVertical
			.create(resources.getString("UI.SpaceRequirements.TITLE"), BorderType.LINE);
		spaceRequirements.addComponent(UIRuleParameter.create(parkingRule.getRpParkingSpaceFilter()));

		// Create the size parameter boxes
		UIContainer sizeParameterBoxes = UIContainerHorizontal.create();
		UIContainer minimumSizesBox = UIContainerVertical
			.create(resources.getString("UI.MinimumSize.TITLE"), BorderType.LINE);
		minimumSizesBox.addComponent(UIRuleParameter.create(parkingRule.getRpMinimumWidth()));
		minimumSizesBox.addComponent(UIRuleParameter.create(parkingRule.getRpMinimumLength()));
		minimumSizesBox.addComponent(UIRuleParameter.create(parkingRule.getRpMinimumHeight()));
		sizeParameterBoxes.addComponent(minimumSizesBox);

		UIContainer maximumSizesBox = UIContainerVertical
			.create(resources.getString("UI.MaximumSize.TITLE"), BorderType.LINE);
		maximumSizesBox.addComponent(UIRuleParameter.create(parkingRule.getRpMaximumWidth()));
		maximumSizesBox.addComponent(UIRuleParameter.create(parkingRule.getRpMaximumLength()));
		maximumSizesBox.addComponent(UIRuleParameter.create(parkingRule.getRpMaximumHeight()));
		sizeParameterBoxes.addComponent(maximumSizesBox);

		spaceRequirements.addComponent(sizeParameterBoxes);
		return spaceRequirements;
	}

	private UIComponent createParkingSpaceAlignmentUIDefinition() {
		// Create the alignment requirements container
		UIContainer alignmentRequirements = UIContainerVertical
			.create(resources.getString("UI.LimitCheckingByAlignment.TITLE"), BorderType.LINE);

		alignmentRequirements.addComponent(UIRuleParameter.create(parkingRule.getRpParkingAisleFilter()));
		alignmentRequirements
			.addComponent(UILabel.create(resources.getString("UI.LimitCheckingByAlignment.DESCRIPTION")));

		UIContainer alignmentSettings = UIContainerHorizontal.create();

		// Add the check boxes for the alignment selection
		UIContainer alignmentCheckBoxes = UIContainerVertical.create();
		UIRuleParameter parallelToAisle = UIRuleParameter.create(parkingRule.getRpParallelOrientation());
		alignmentCheckBoxes.addComponent(parallelToAisle);
		UIRuleParameter perpendicularToAisle = UIRuleParameter.create(parkingRule.getRpPerpendicularOrientation());
		alignmentCheckBoxes.addComponent(perpendicularToAisle);
		UIRuleParameter angleToAisle = UIRuleParameter.create(parkingRule.getRpAngledOrientation());
		alignmentCheckBoxes.addComponent(angleToAisle);
		alignmentSettings.addComponent(alignmentCheckBoxes);

		// Add the images and set them to be enabled based on the check boxes
		UIImage parallelToAisleImage = UIImage.create(resources.getImageUrl("alignment_parallel_to_aisle.png"));
		parallelToAisleImage.setEnabler(parallelToAisle);
		alignmentSettings.addComponent(parallelToAisleImage);

		UIImage perpendicularToAisleImage = UIImage
			.create(resources.getImageUrl("alignment_perpendicular_to_aisle.png"));
		perpendicularToAisleImage.setEnabler(perpendicularToAisle);
		alignmentSettings.addComponent(perpendicularToAisleImage);

		UIImage angleToAisleImage = UIImage.create(resources.getImageUrl("alignment_angle_to_aisle.png"));
		angleToAisleImage.setEnabler(angleToAisle);
		alignmentSettings.addComponent(angleToAisleImage);

		alignmentRequirements.addComponent(alignmentSettings);

		return alignmentRequirements;
	}

	private UIComponent createObstructionCheckingUIDefinition() {
		// Create the Limit Checking by obstructions container
		UIContainer limitObstructions = UIContainerVertical
			.create(resources.getString("UI.LimitCheckingByObstructions.TITLE"), BorderType.LINE);
		limitObstructions.addComponent(UIRuleParameter.create(parkingRule.getRpParkingObstructionsFilter()));
		limitObstructions.addComponent(createEndObstructionsContainer());
		limitObstructions.addComponent(createSideObstructionsContainer());

		return limitObstructions;
	}

	private UIContainer createEndObstructionsContainer() {
		UIContainer endObstructions = UIContainerVertical
			.create(resources.getString("UI.EndObstructions.TITLE"), BorderType.LINE);
		endObstructions.addComponent(UILabel.create(resources.getString("UI.EndObstructions.DESCRIPTION")));

		UIContainer endObstructionSettings = UIContainerHorizontal.create();

		// Add the selection parameters
		UIContainer endObstructionSettingsPane = UIContainerVertical.create();
		UIRuleParameter neitherEndObstructed = UIRuleParameter.create(parkingRule.getRpEndObstructionNo());
		endObstructionSettingsPane.addComponent(neitherEndObstructed);
		UIRuleParameter oneEndObstructed = UIRuleParameter.create(parkingRule.getRpEndObstructionOne());
		endObstructionSettingsPane.addComponent(oneEndObstructed);
		UIRuleParameter bothEndsObstructed = UIRuleParameter.create(parkingRule.getRpEndObstructionBoth());
		endObstructionSettingsPane.addComponent(bothEndsObstructed);
		endObstructionSettings.addComponent(endObstructionSettingsPane);

		// Add the images and set them to be enabled based on the check boxes
		UIImage neitherEndImage = UIImage.create(resources.getImageUrl("end_obstructions_neither.png"));
		neitherEndImage.setEnabler(neitherEndObstructed);
		endObstructionSettings.addComponent(neitherEndImage);
		UIImage oneEndImage = UIImage.create(resources.getImageUrl("end_obstructions_one.png"));
		oneEndImage.setEnabler(oneEndObstructed);
		endObstructionSettings.addComponent(oneEndImage);
		UIImage bothEndImage = UIImage.create(resources.getImageUrl("end_obstructions_both.png"));
		bothEndImage.setEnabler(bothEndsObstructed);
		endObstructionSettings.addComponent(bothEndImage);
		endObstructions.addComponent(endObstructionSettings);
		return endObstructions;
	}

	private UIContainer createSideObstructionsContainer() {
		UIContainer sideObstructions = UIContainerVertical
			.create(resources.getString("UI.SideObstructions.TITLE"), BorderType.LINE);
		sideObstructions.addComponent(UILabel.create(resources.getString("UI.SideObstructions.DESCRIPTION")));

		// Add the container for settings check boxes and images.
		UIContainer sideObstructionSettings = UIContainerHorizontal.create();

		// Create a container for the check box pane on the left.
		UIContainer sideObstructionSettingsPane = UIContainerVertical.create();
		UIRuleParameter neitherSideObstructed = UIRuleParameter.create(parkingRule.getRpSideObstructionNo());
		sideObstructionSettingsPane.addComponent(neitherSideObstructed);
		UIRuleParameter onSideObstructed = UIRuleParameter.create(parkingRule.getRpSideObstructionOne());
		sideObstructionSettingsPane.addComponent(onSideObstructed);
		UIRuleParameter bothSidesObstructed = UIRuleParameter.create(parkingRule.getRpSideObstructionBoth());
		sideObstructionSettingsPane.addComponent(bothSidesObstructed);
		sideObstructionSettings.addComponent(sideObstructionSettingsPane);

		// Create a container for the images and the obstruction free zone
		// setting.
		UIContainer rightHandSideContainer = UIContainerVertical.create();

		// Create the images and set them to follow the state of the parameter
		// states
		UIContainer sideObstructionImagePane = UIContainerHorizontal.create();

		UIImage neitherSideImage = UIImage.create(resources.getImageUrl("side_obstructions_neither.png"));
		neitherSideImage.setEnabler(neitherSideObstructed);
		sideObstructionImagePane.addComponent(neitherSideImage);
		UIImage oneSideImage = UIImage.create(resources.getImageUrl("side_obstructions_oneside.png"));
		oneSideImage.setEnabler(onSideObstructed);
		sideObstructionImagePane.addComponent(oneSideImage);
		UIImage bothSidesImage = UIImage.create(resources.getImageUrl("side_obstructions_both.png"));
		bothSidesImage.setEnabler(bothSidesObstructed);
		sideObstructionImagePane.addComponent(bothSidesImage);
		rightHandSideContainer.addComponent(sideObstructionImagePane);

		// Create the container for the Obstruction free zone settings.
		UIContainer obstructionFreeZoneSettings = UIContainerVertical.create();

		// Place the rpUseObstructionFreeZone parameter in a container with
		// padding containers on sides.
		UIContainer useObstructionFreeZoneContainer = UIContainerHorizontal.create();
		// Add padding container to the left.
		useObstructionFreeZoneContainer.addComponent(UIContainerVertical.create());
		UIRuleParameter useObstructionFreeZone = UIRuleParameter.create(parkingRule.getRpUseObstructionFreeZone());
		// Add padding container to the right.
		useObstructionFreeZoneContainer.addComponent(UIContainerVertical.create());
		useObstructionFreeZoneContainer.addComponent(useObstructionFreeZone);
		obstructionFreeZoneSettings.addComponent(useObstructionFreeZoneContainer);

		// Place the rpObstructionFreeZone parameter in a container with
		// padding containers on sides.
		UIContainer obstructionFreeZoneLengthContainer = UIContainerHorizontal.create();
		// Add padding container to the left.
		obstructionFreeZoneLengthContainer.addComponent(UIContainerVertical.create());
		UIRuleParameter obstructionFreeZoneLength = UIRuleParameter.create(parkingRule.getRpObstructionFreeZone());
		obstructionFreeZoneLength.setEnabler(useObstructionFreeZone);
		obstructionFreeZoneLengthContainer.addComponent(obstructionFreeZoneLength);
		// Add padding container to the right.
		obstructionFreeZoneLengthContainer.addComponent(UIContainerVertical.create());
		obstructionFreeZoneSettings.addComponent(obstructionFreeZoneLengthContainer);
		rightHandSideContainer.addComponent(obstructionFreeZoneSettings);

		sideObstructionSettings.addComponent(rightHandSideContainer);
		sideObstructions.addComponent(sideObstructionSettings);

		return sideObstructions;
	}
}
