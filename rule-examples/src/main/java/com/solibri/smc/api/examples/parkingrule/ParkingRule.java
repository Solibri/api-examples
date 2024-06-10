package com.solibri.smc.api.examples.parkingrule;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.solibri.geometry.linearalgebra.MVector3d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.linearalgebra.Vector3d;
import com.solibri.geometry.primitive3d.AABB3d;
import com.solibri.geometry.primitive3d.Segment3d;
import com.solibri.smc.api.checking.BooleanParameter;
import com.solibri.smc.api.checking.ComponentSelector;
import com.solibri.smc.api.checking.ConcurrentRule;
import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.PreCheckResult;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.footprints.Footprint;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.Model;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.visualization.ARGBColor;
import com.solibri.smc.api.visualization.Visualization;
import com.solibri.smc.api.visualization.VisualizationItem;

public final class ParkingRule extends ConcurrentRule {

	private final RuleParameters params = RuleParameters.of(this);

	private final FilterParameter rpParkingSpaceFilter = this.getDefaultFilterParameter();
	private final FilterParameter rpParkingAisleFilter = params.createFilter("rpParkingAisleFilter");
	private final FilterParameter rpParkingObstructionsFilter = params.createFilter("rpParkingObstructionsFilter");

	private final DoubleParameter rpMinimumWidth = params.createDouble("rpMinimumWidth", PropertyType.LENGTH);
	private final DoubleParameter rpMinimumLength = params.createDouble("rpMinimumLength", PropertyType.LENGTH);
	private final DoubleParameter rpMinimumHeight = params.createDouble("rpMinimumHeight", PropertyType.LENGTH);

	private final DoubleParameter rpMaximumWidth = params.createDouble("rpMaximumWidth", PropertyType.LENGTH);
	private final DoubleParameter rpMaximumLength = params.createDouble("rpMaximumLength", PropertyType.LENGTH);
	private final DoubleParameter rpMaximumHeight = params.createDouble("rpMaximumHeight", PropertyType.LENGTH);

	private final BooleanParameter rpEndObstructionNo = params.createBoolean("rpEndObstructionNo");
	private final BooleanParameter rpEndObstructionOne = params.createBoolean("rpEndObstructionOne");
	private final BooleanParameter rpEndObstructionBoth = params.createBoolean("rpEndObstructionBoth");

	private final BooleanParameter rpSideObstructionNo = params.createBoolean("rpSideObstructionNo");
	private final BooleanParameter rpSideObstructionOne = params.createBoolean("rpSideObstructionOne");
	private final BooleanParameter rpSideObstructionBoth = params.createBoolean("rpSideObstructionBoth");

	private final BooleanParameter rpParallelOrientation = params.createBoolean("rpParallelOrientation");
	private final BooleanParameter rpPerpendicularOrientation = params.createBoolean("rpPerpendicularOrientation");
	private final BooleanParameter rpAngledOrientation = params.createBoolean("rpAngledOrientation");

	private final BooleanParameter rpUseObstructionFreeZone = params.createBoolean("rpUseObstructionFreeZone");
	private final DoubleParameter rpObstructionFreeZone = params
		.createDouble("rpObstructionFreeZone", PropertyType.LENGTH);


	private final ParkingRuleUIDefinition uiDefinition = new ParkingRuleUIDefinition(this);

	private final RuleResources resources = RuleResources.of(this);
	private Model targetModel;

	@Override
	public PreCheckResult preCheck() {
		return PreCheckResult.createRelevant();
	}

	@Override
	public PreCheckResult preCheck(ComponentSelector components) {
		targetModel = components.getTargetModel();
		return super.preCheck(components);
	}
	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		/*
		 * Check only parking spaces which end obstruction, side obstruction and
		 * orientation are accepted by the parameters. If the parking space
		 * doesn't have valid footprints and bounds or the largest inscribed
		 * rectangle wasn't found, a cannot check result will be created.
		 */
		OrientationChecking orientationChecking = new OrientationChecking(targetModel,
			getRpParkingAisleFilter().getValue());
		ParkingSpace parkingSpace = createParkingSpace(component);
		ObstructionChecking obstructionChecking = new ObstructionChecking(targetModel, getRpMinimumWidth().getValue(),
			getRpParkingObstructionsFilter().getValue());
		if (parkingSpace == null || !parkingSpace.isValid()) {
			return Collections.singleton(
				createCannotCheckResult(component, resultFactory));
		} else if (filterByObstructions(parkingSpace, obstructionChecking) && filterByOrientation(
			orientationChecking.findOrientation(parkingSpace))) {
			return checkParkingSpaceSize(component, parkingSpace, resultFactory, obstructionChecking)
				.map(Collections::singleton).orElse(Collections.emptySet());
		}

		return Collections.emptyList();
	}

	@Override
	public void postCheck() {
	}

	ParkingSpace createParkingSpace(Component entity) {
		ParkingSpace parkingSpace = null;

		Footprint footprint = entity.getFootprint();
		AABB3d aabb = entity.getBoundingBox();

		if (footprint != null && aabb != null && !aabb.isDegenerate()) {
			Vector2d[] footprintPolygon = footprint.getOutline().getVertices()
				.toArray(new Vector2d[footprint.getOutline().getVertexCount()]);
			Vector3d[] largestInscribedRectangle = MaximumRectangle.findMaximumRectangle(footprintPolygon);
			if (largestInscribedRectangle != null) {
				parkingSpace = new ParkingSpace(entity, footprintPolygon, largestInscribedRectangle, aabb);
			}
		}

		return parkingSpace;
	}

	private Optional<Result> checkParkingSpaceSize(Component entity, ParkingSpace size, ResultFactory resultFactory,
		ObstructionChecking obstructionChecking) {
		List<ParkingSpaceSizeRequirement.Violation> violations = new ArrayList<>();
		ParkingSpaceSizeRequirement sizeRequirement = new ParkingSpaceSizeRequirement();

		sizeRequirement.checkRequirement(ParkingSpaceSizeRequirement.Requirement.MaximumWidth, getRpMaximumWidth(),
			size.getWidth(), violations);
		sizeRequirement.checkRequirement(ParkingSpaceSizeRequirement.Requirement.MinimumWidth, getRpMinimumWidth(),
			size.getWidth(), violations);

		sizeRequirement.checkRequirement(ParkingSpaceSizeRequirement.Requirement.MaximumLength, getRpMaximumLength(),
			size.getLength(), violations);
		sizeRequirement.checkRequirement(ParkingSpaceSizeRequirement.Requirement.MinimumLength, getRpMinimumLength(),
			size.getLength(), violations);

		sizeRequirement.checkRequirement(ParkingSpaceSizeRequirement.Requirement.MaximumHeight, getRpMaximumHeight(),
			size.getHeight(), violations);
		sizeRequirement.checkRequirement(ParkingSpaceSizeRequirement.Requirement.MinimumHeight, getRpMinimumHeight(),
			size.getHeight(), violations);

		// create a result when the size requirements are violated
		if (!violations.isEmpty()) {
			return Optional.of(createWrongSizeResult(entity, size, violations, resultFactory, obstructionChecking));
		}
		return Optional.empty();
	}

	private Result createCannotCheckResult(Component entity, ResultFactory resultFactory) {
		String displayName = entity.getName();
		String resultKey = entity.getGUID();
		String name = resources.getString("Result.CannotCheck.Name", displayName);
		String description = resources.getString("Result.CannotCheck.Description", displayName);

		return resultFactory
			.create(name, description)
			.withCustomUniqueKey(resultKey);
	}

	private Result createWrongSizeResult(Component entity,
		ParkingSpace parkingSpace,
		List<ParkingSpaceSizeRequirement.Violation> violations,
		ResultFactory resultFactory,
		ObstructionChecking obstructionChecking) {

		boolean isWidthViolation = false;
		boolean isHeightViolation = false;
		boolean isLengthViolation = false;

		String resultKey = entity.getGUID();
		String displayName = entity.getName();
		String name = resources.getString("Result.WrongSize.Name", displayName);

		StringBuilder messageBuilder = new StringBuilder(
			resources.getString("Result.WrongSize.Description", displayName));

		Format lengthFormat = PropertyType.LENGTH.getFormat();
		for (ParkingSpaceSizeRequirement.Violation violation : violations) {
			String limitLength = lengthFormat.format(violation.getLimitValue());
			String valueLength = lengthFormat.format(violation.getValue());
			String message = resources.getString(violation.getRequirement().name(), valueLength, limitLength);
			messageBuilder.append("<br>").append("- ").append(message);

			switch (violation.getRequirement()) {
			case MaximumHeight:
			case MinimumHeight:
				isHeightViolation = true;
				break;

			case MaximumLength:
			case MinimumLength:
				isLengthViolation = true;
				break;

			case MaximumWidth:
			case MinimumWidth:
				isWidthViolation = true;
				break;

			default:
				break;
			}
		}

		final boolean finalIsWidthViolation = isWidthViolation;
		final boolean finalIsHeightViolation = isHeightViolation;
		final boolean finalIsLengthViolation = isLengthViolation;

		return resultFactory.create(name, messageBuilder.toString())
			.withCustomUniqueKey(resultKey)
			.withVisualization(visualization ->
				createSizeResultVisualization(
					entity,
					parkingSpace,
					finalIsWidthViolation,
					finalIsHeightViolation,
					finalIsLengthViolation,
				visualization,
				obstructionChecking));

	}

	private void createSizeResultVisualization(Component entity, ParkingSpace parkingSpace, boolean isWidthViolation,
		boolean isHeightViolation,
		boolean isLengthViolation,
		Visualization inputVisualization,
		ObstructionChecking obstructionChecking) {

		VisualizationItem parkingSpaceVisualization = VisualizationItem
			.createArea(parkingSpace.getRectangleArea(), parkingSpace.getElevation());

		inputVisualization.addVisualizationItem(parkingSpaceVisualization);

		if (isWidthViolation) {
			Segment3d shorterSegment = parkingSpace.getShorterSegment();
			List<VisualizationItem> widthDimension =
				VisualizationItem.createDimension(shorterSegment.getStartPoint(), shorterSegment.getEndPoint());
			inputVisualization.addVisualizationItems(widthDimension);
		}

		if (isLengthViolation) {
			Segment3d longerSegment = parkingSpace.getLongerSegment();
			List<VisualizationItem> lengthDimension =
				VisualizationItem.createDimension(longerSegment.getStartPoint(), longerSegment.getEndPoint());
			inputVisualization.addVisualizationItems(lengthDimension);
		}

		if (isHeightViolation) {
			Vector3d spacePoint = parkingSpace.getLongerSegment().getStartPoint();
			MVector3d upperSpacePoint = MVector3d.create(spacePoint);
			upperSpacePoint.addInPlace(Vector3d.UNIT_Z.scale(parkingSpace.getHeight()));
			List<VisualizationItem> heightDimension = VisualizationItem.createDimension(spacePoint, upperSpacePoint);
			inputVisualization.addVisualizationItems(heightDimension);
		}

		final double transparency = 0.7;
		inputVisualization.addComponent(entity, transparency);

		final ARGBColor red = ARGBColor.create(255, 0, 0, 255);

		for (Component potentialObs : obstructionChecking.getPotentialObstructions()) {
			if (obstructionChecking.isObstructionInsideParkingSpace(potentialObs)) {
				inputVisualization.addComponentWithColor(potentialObs, red);
			} else {
				inputVisualization.addComponent(potentialObs, transparency);
			}
		}
	}

	private boolean filterByOrientation(OrientationChecking.Orientation orientation) {
		Boolean acceptParallel = getRpParallelOrientation().getValue();
		Boolean acceptAngled = getRpAngledOrientation().getValue();
		Boolean acceptPerpendicular = getRpPerpendicularOrientation().getValue();

		if (acceptParallel || acceptAngled || acceptPerpendicular) {
			switch (orientation) {
			case Parallel:
				return acceptParallel;

			case Angled:
				return acceptAngled;

			case Perpendicular:
				return acceptPerpendicular;

			case Undefined:
			default:
				break;
			}
		}

		return true;
	}

	private boolean filterByObstructions(ParkingSpace parkingSpace, ObstructionChecking obstructionChecking) {

		boolean hasSelectedEndObstruction = true;
		boolean hasSelectedSideObstruction = true;
		obstructionChecking.findParkingSpaceObstructions(parkingSpace);

		if (getRpSideObstructionNo().getValue() || getRpSideObstructionOne().getValue() || getRpSideObstructionBoth()
			.getValue()) {
			if (getRpUseObstructionFreeZone().getValue()) {
				hasSelectedSideObstruction = isSideObstructionOfSelectedType(
					obstructionChecking.getFreeZoneObstructionType(getRpObstructionFreeZone().getValue()));
			} else {
				hasSelectedSideObstruction = isSideObstructionOfSelectedType(
					obstructionChecking.getSideObstructionType());
			}
		}

		if (getRpEndObstructionNo().getValue() || getRpEndObstructionOne().getValue() || getRpEndObstructionBoth()
			.getValue()) {
			hasSelectedEndObstruction = isEndObstructionOfSelectedType(obstructionChecking.getEndObstructionType());
		}

		return hasSelectedSideObstruction && hasSelectedEndObstruction;
	}

	private boolean isSideObstructionOfSelectedType(ObstructionChecking.ObstructionType obstructionType) {
		switch (obstructionType) {
		case NONE:
			return getRpSideObstructionNo().getValue();
		case ONE:
			return getRpSideObstructionOne().getValue();
		case BOTH:
			return getRpSideObstructionBoth().getValue();
		default:
			break;
		}
		return true;
	}

	private boolean isEndObstructionOfSelectedType(ObstructionChecking.ObstructionType obstructionType) {
		switch (obstructionType) {
		case NONE:
			return getRpEndObstructionNo().getValue();
		case ONE:
			return getRpEndObstructionOne().getValue();
		case BOTH:
			return getRpEndObstructionBoth().getValue();
		default:
			break;
		}
		return true;
	}

	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}

	FilterParameter getRpParkingSpaceFilter() {
		return rpParkingSpaceFilter;
	}
	FilterParameter getRpParkingAisleFilter() {
		return rpParkingAisleFilter;
	}
	FilterParameter getRpParkingObstructionsFilter() {
		return rpParkingObstructionsFilter;
	}
	DoubleParameter getRpMinimumWidth() {
		return rpMinimumWidth;
	}
	DoubleParameter getRpMinimumLength() {
		return rpMinimumLength;
	}
	DoubleParameter getRpMinimumHeight() {
		return rpMinimumHeight;
	}
	DoubleParameter getRpMaximumWidth() {
		return rpMaximumWidth;
	}
	DoubleParameter getRpMaximumLength() {
		return rpMaximumLength;
	}
	DoubleParameter getRpMaximumHeight() {
		return rpMaximumHeight;
	}
	BooleanParameter getRpEndObstructionNo() {
		return rpEndObstructionNo;
	}
	BooleanParameter getRpEndObstructionOne() {
		return rpEndObstructionOne;
	}
	BooleanParameter getRpEndObstructionBoth() {
		return rpEndObstructionBoth;
	}
	BooleanParameter getRpSideObstructionNo() {
		return rpSideObstructionNo;
	}
	BooleanParameter getRpSideObstructionOne() {
		return rpSideObstructionOne;
	}
	BooleanParameter getRpSideObstructionBoth() {
		return rpSideObstructionBoth;
	}
	BooleanParameter getRpParallelOrientation() {
		return rpParallelOrientation;
	}
	BooleanParameter getRpPerpendicularOrientation() {
		return rpPerpendicularOrientation;
	}
	BooleanParameter getRpAngledOrientation() {
		return rpAngledOrientation;
	}
	BooleanParameter getRpUseObstructionFreeZone() {
		return rpUseObstructionFreeZone;
	}
	DoubleParameter getRpObstructionFreeZone() {
		return rpObstructionFreeZone;
	}

	@Override
	public Map<String, String> getParameterTemplateKeyToIdMap() {
		Map<String, String> parameterTemplateKey = new HashMap<>();
		parameterTemplateKey.put("PARAM_PARKING_SPACES_TO_CHECK", "rpComponentFilter");
		parameterTemplateKey.put("PARAM_AISLE_COMP_TO_CHECK", "rpParkingAisleFilter");
		parameterTemplateKey.put("PARAM_OBSTRUCTION_COMP_TO_CHECK", "rpParkingObstructionsFilter");
		parameterTemplateKey.put("PARAM_MIN_WIDTH", "rpMinimumWidth");
		parameterTemplateKey.put("PARAM_MIN_LENGTH", "rpMinimumLength");
		parameterTemplateKey.put("PARAM_MIN_HEIGHT", "rpMinimumHeight");
		parameterTemplateKey.put("PARAM_MAX_WIDTH", "rpMaximumWidth");
		parameterTemplateKey.put("PARAM_MAX_LENGTH", "rpMaximumLength");
		parameterTemplateKey.put("PARAM_MAX_HEIGHT", "rpMaximumHeight");
		parameterTemplateKey.put("PARAM_NEITHER_END_OBSTRUCTED", "rpEndObstructionNo");
		parameterTemplateKey.put("PARAM_ONE_END_OBSTRUCTED", "rpEndObstructionOne");
		parameterTemplateKey.put("PARAM_BOTH_ENDS_OBSTRUCTED", "rpEndObstructionBoth");
		parameterTemplateKey.put("PARAM_NEITHER_SIDE_OBSTRUCTED", "rpSideObstructionNo");
		parameterTemplateKey.put("PARAM_ONE_SIDE_OBSTRUCTED", "rpSideObstructionOne");
		parameterTemplateKey.put("PARAM_BOTH_SIDE_OBSTRUCTED", "rpSideObstructionBoth");
		parameterTemplateKey.put("PARAM_PARALLEL_TO_AISLE", "rpParallelOrientation");
		parameterTemplateKey.put("PARAM_PERPENDICULAR_TO_AISLE", "rpPerpendicularOrientation");
		parameterTemplateKey.put("PARAM_AT_AN_ANGLE_TO_AISLE", "rpAngledOrientation");
		parameterTemplateKey.put("PARAM_CHECK_MID_SPACE_OBSTRUCTION_FREE_ZONE", "rpUseObstructionFreeZone");
		parameterTemplateKey.put("PARAM_MID_SPACE_OBSTRUCTION_FREE_ZONE_LENGTH", "rpObstructionFreeZone");
		return parameterTemplateKey;
	}
}
