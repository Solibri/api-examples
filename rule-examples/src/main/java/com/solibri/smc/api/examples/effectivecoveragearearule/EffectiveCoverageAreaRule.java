package com.solibri.smc.api.examples.effectivecoveragearearule;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.BooleanParameter;
import com.solibri.smc.api.checking.ConcurrentRule;
import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.EnumerationParameter;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.PropertyReferenceParameter;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyReference;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.visualization.VisualizationItem;

/**
 * This rule provides multiple way to check the Effective coverage area.
 */
public class EffectiveCoverageAreaRule extends ConcurrentRule {

	private static final String DECIMAL_FORMAT_VALUE = "#.##";

	private static final double ONE_MILLIMETER_IN_METERS = 0.001;

	private final RuleParameters params = RuleParameters.of(this);

	final FilterParameter rpSpacesToCheck = this.getDefaultFilterParameter();

	final FilterParameter rpEffectSources = params.createFilter("rpEffectSources");

	final DoubleParameter rpEffectRange = params.createDouble("rpEffectParameters.EffectRange",
		PropertyType.LENGTH);

	final BooleanParameter rpPropagateToConnectedSpaces = params
		.createBoolean("rpEffectParameters.PropagateToConnectedSpaces");

	final DoubleParameter rpMinimumCoverage = params.createDouble("rpMinimumCoverage",
		PropertyType.PERCENTAGE);

	final EnumerationParameter rpOcclusionAndBounds = params.createEnumeration("rpOcclusionAndBounds",
		Stream.of(EffectiveCoverageAreaBehaviour.values()).map(EffectiveCoverageAreaBehaviour::getPropertyKey)
			.collect(Collectors.toList()));

	final DoubleParameter rpRequiredMinimumRatio = params.createDouble("rpRequiredMinimumRatio",
		PropertyType.PERCENTAGE);

	final PropertyReferenceParameter rpEffectSourcePropertyReference = params
		.createPropertyReference("rpEffectSourcePropertyReference");

	final PropertyReferenceParameter rpEffectSourceMultiplier = params
		.createPropertyReference("rpEffectSourceMultiplier");

	final PropertyReferenceParameter rpAreaPropertyReference = params
		.createPropertyReference("rpAreaPropertyReference");

	/*
	 * This class collects the information about the 'Ratio of Property Values'
	 * checking and it's content is used for result.
	 */
	private static class PropertyValuesRatioCheckingData {
		/*
		 * The description of each effect source that are involved in checking
		 * of investigated area are stored into sourcesDescription.
		 */
		String sourcesDescription = "";
		/*
		 * Set as true if the checking contains at least one effect source that
		 * have one or more property value missing.
		 */
		boolean hasSourceWithMissingPropertyValue;
		/*
		 * Set as true if the checking contains at least one effect source that
		 * have all the needed property values.
		 */
		boolean hasSourceWithCorrectPropertyValue;
	}

	/*
	 * The uiDefinition can only be created after all parameters have been
	 * created.
	 */
	private final EffectiveCoverageAreaRuleUIDefinition uiDefinition = new EffectiveCoverageAreaRuleUIDefinition(this);

	private final RuleResources resources = RuleResources.of(this);

	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {

		Collection<Component> effectSources = getEffectSourcesNear(component);
		EffectiveCoverageChecking checking = getCheck(component, effectSources);
		Collection<Result> results = new ArrayList<>();
		checking.checkViolations().ifPresent(violation -> results.add(createResult(violation, resultFactory)));
		Optional<Result> propertyValueRatioResult = checkPropertyValueRatio(component, effectSources, resultFactory);
		propertyValueRatioResult.ifPresent(results::add);
		return results;
	}

	/**
	 * Checks the property value ratios for the effective coverage area
	 * components. Check is only run if rpRequiredMinimumRatio is positive.
	 *
	 * @param spaceEntity the current space entity
	 * @param effectSources the found effect sources for the given space entity
	 * @param resultFactory the result factory
	 *
	 * @return the checking result
	 */
	private Optional<Result> checkPropertyValueRatio(Component spaceEntity, Collection<Component> effectSources,
													 ResultFactory resultFactory) {
		if (rpAreaPropertyReference != null && rpAreaPropertyReference.getValue() != null
			&& rpEffectSourcePropertyReference != null
			&& rpEffectSourcePropertyReference.getValue() != null && rpRequiredMinimumRatio.getValue() > 0.0) {

			double spaceTotal = spaceEntity.<Double>getPropertyValue(rpAreaPropertyReference.getValue()).orElse(0.0);
			String spaceDescription = getSpaceDescription(spaceEntity, spaceTotal);
			String issueName = "";
			String issueDescription = "";

			if (spaceTotal == 0.0) {

				// Can't calculate the ratio: Space is missing value
				issueName = resources.getString("Result.MissingSpacePropertyValue.Name");
				issueDescription = resources
					.getString("Result.MissingSpacePropertyValue.Description", spaceDescription);

			} else {

				double sourceTotal = getPropertyValueSum(effectSources);
				boolean isPropertyValueRatioTooSmall = sourceTotal / spaceTotal < rpRequiredMinimumRatio.getValue();

				PropertyValuesRatioCheckingData propertyValuesRatioCheckingData = getSourceDescription(effectSources,
					spaceTotal);
				boolean hasSourceWithMissingPropertyValue = propertyValuesRatioCheckingData.hasSourceWithMissingPropertyValue;
				boolean hasSourceWithCorrectPropertyValue = propertyValuesRatioCheckingData.hasSourceWithCorrectPropertyValue;
				String sourcesDescription = propertyValuesRatioCheckingData.sourcesDescription;

				Format percentageFormat = PropertyType.PERCENTAGE.getFormat();
				String percentage = percentageFormat.format(sourceTotal / spaceTotal);
				String reguiredPercentage = percentageFormat.format(rpRequiredMinimumRatio.getValue());

				if (!hasSourceWithMissingPropertyValue && !hasSourceWithCorrectPropertyValue) {
					/*
					 * Can't calculate the ratio: Space has value: Doesn't
					 * contain source
					 */
					issueName = resources.getString("Result.MissingSources.Name");
					issueDescription = resources.getString("Result.MissingSources.Description", spaceDescription);
				} else if (hasSourceWithMissingPropertyValue && !hasSourceWithCorrectPropertyValue) {
					/*
					 * Can't calculate the ratio: Space has value: Contain
					 * sources: Each effect source have missing properties
					 */
					issueName = resources.getString("Result.EachSourceAreMissingParameters.Name");
					issueDescription = resources
						.getString("Result.EachSourceAreMissingParameters.Description", spaceDescription,
							sourcesDescription);
				} else if (isPropertyValueRatioTooSmall && hasSourceWithMissingPropertyValue
					&& hasSourceWithCorrectPropertyValue) {
					/*
					 * Can calculate Ratio: Ratio was too small: Some sources
					 * contain missing property values and some have all the
					 * needed values
					 */
					issueName = resources
						.getString("Result.TooSmallPropertyValueRatioAndSomeSourceAreMissingPropertyValues.Name");
					issueDescription = resources.getString(
						"Result.TooSmallPropertyValueRatioAndSomeSourceAreMissingPropertyValues.Description",
						spaceDescription, sourcesDescription, percentage, reguiredPercentage);
				} else if (isPropertyValueRatioTooSmall && !hasSourceWithMissingPropertyValue
					&& hasSourceWithCorrectPropertyValue) {
					/*
					 * Can calculate Ratio: Ratio was too small: Didn't contain
					 * missing property values
					 */
					issueName = resources.getString("Result.TooSmallPropertyValueRatio.Name");
					issueDescription = resources
						.getString("Result.TooSmallPropertyValueRatio.Description", spaceDescription,
							sourcesDescription, percentage, reguiredPercentage);
				} else if (!isPropertyValueRatioTooSmall && hasSourceWithMissingPropertyValue
					&& hasSourceWithCorrectPropertyValue) {
					/*
					 * Can calculate Ratio: Ratio was ok: Sources contain
					 * missing property values but some have all the needed
					 * values
					 */
					issueName = resources.getString("Result.MissingSourcePropertyValues.Name");
					issueDescription = resources
						.getString("Result.MissingSourcePropertyValues.Description", spaceDescription,
							sourcesDescription, percentage, reguiredPercentage);
				}
			}

			if (!issueName.isEmpty()) {
				return Optional.of(
					addPropertyValueRatioResult(
						spaceEntity,
						effectSources,
						issueName,
						issueDescription,
						resultFactory));
			}
		}
		return Optional.empty();
	}

	private Result addPropertyValueRatioResult(Component spaceEntity, Collection<Component> effectSources,
											   String issueName,
											   String issueDescription, ResultFactory resultFactory) {
		return resultFactory
			.create(issueName, issueDescription)
			.withCustomUniqueKey(spaceEntity.getGUID() + "PropertyValueRatio")
			.withInvolvedComponents(effectSources)
			.withVisualization(visualization -> {
				visualization.addComponentsWithColor(effectSources, EffectiveAreaVisualizationColor.SOURCE_RED);
				visualization
					.addComponentWithColor(spaceEntity, EffectiveAreaVisualizationColor.SPACE_GREEN_TRANSPARENT);
			});
	}

	private double getPropertyValueSum(Collection<Component> effectSources) {
		PropertyReference multiplierReference = rpEffectSourceMultiplier.getValue();
		double sourceTotal = 0.0;
		for (Component source : effectSources) {
			Optional<Double> effectSourcePropertyValue = source
				.getPropertyValue(rpEffectSourcePropertyReference.getValue());
			if (effectSourcePropertyValue.isPresent()) {
				double sourceValue = effectSourcePropertyValue.get();
				if (sourceValue != 0.0) {
					// Check if the multiplier value is needed
					if (multiplierReference != null) {
						Optional<Double> effectSourceMultiplierValue = source.getPropertyValue(multiplierReference);
						if (effectSourceMultiplierValue.isPresent() && effectSourceMultiplierValue.get() != 0.0) {
							sourceTotal += sourceValue * effectSourceMultiplierValue.get();
						}
					} else {
						sourceTotal += sourceValue;
					}
				}
			}
		}
		return sourceTotal;
	}

	private String getSpaceDescription(Component spaceEntity, double spaceTotal) {
		String name = spaceEntity.getName();
		if (spaceTotal == 0.0) {
			String missingContent = resources.getString("Result.MissingProperty", resources.getString("Result.Area"),
				rpAreaPropertyReference.getValue().toString());
			return resources.getString("Result.ListItem",
				resources.getString("Result.SourceAndMissingProperties", name, missingContent));
		}
		return resources.getString("Result.ListItem", name);
	}

	private PropertyValuesRatioCheckingData getSourceDescription(Collection<Component> effectSources,
																 double spaceTotal) {
		PropertyValuesRatioCheckingData propertyValuesRatioCheckingData = new PropertyValuesRatioCheckingData();
		effectSources.stream()
			.forEach(
				entity -> getDisplayNameAndPercentage(spaceTotal, entity, propertyValuesRatioCheckingData));
		return propertyValuesRatioCheckingData;
	}

	private void getDisplayNameAndPercentage(double spaceTotal, Component entity,
											 PropertyValuesRatioCheckingData propertyValuesRatioCheckingData) {
		String source = entity.getName();

		String missingContent = "";

		Optional<Double> effectSourcePropertyValue = entity
			.getPropertyValue(rpEffectSourcePropertyReference.getValue());
		double value = 0.0;
		boolean missingValue = true;
		if (effectSourcePropertyValue.isPresent() && effectSourcePropertyValue.get() != 0.0) {
			missingValue = false;
			value = effectSourcePropertyValue.get();
		}
		// Check if the effect source value is missing
		if (missingValue) {
			missingContent =
				" " + resources.getString("Result.MissingProperty", resources.getString("Result.EffectSource"),
					rpEffectSourcePropertyReference.getValue().toString());
		}

		// Check if the multiplier value is needed
		PropertyReference multiplierReference = rpEffectSourceMultiplier.getValue();
		if (multiplierReference != null) {
			Optional<Double> effectSourceMultiplierValue = entity.getPropertyValue(multiplierReference);
			boolean missingMultiplier = true;
			if (effectSourceMultiplierValue.isPresent() && effectSourceMultiplierValue.get() != 0.0) {
				missingMultiplier = false;
				value *= effectSourceMultiplierValue.get();
			}
			// Check if the multiplier value is missing
			if (missingMultiplier) {
				missingContent += " "
					+ resources
					.getString("Result.MissingProperty", resources.getString("Result.EffectSourceMultiplier"),
						rpEffectSourceMultiplier.getValue().toString());
			}
		}

		if (!missingContent.isEmpty()) {
			propertyValuesRatioCheckingData.hasSourceWithMissingPropertyValue = true;
			// Description of the source is containing source name and missing property value information
			propertyValuesRatioCheckingData.sourcesDescription += resources.getString("Result.ListItem",
				resources.getString("Result.SourceAndMissingProperties", source, missingContent));
		} else {
			propertyValuesRatioCheckingData.hasSourceWithCorrectPropertyValue = true;
			// Description of the source is containing source name and percentage value
			Format percentageFormat = PropertyType.PERCENTAGE.getFormat();
			String percentage = percentageFormat.format(value / spaceTotal);
			propertyValuesRatioCheckingData.sourcesDescription += resources.getString("Result.ListItem",
				resources.getString("Result.SourceAndPercentage", source, percentage));
		}
	}

	/**
	 * Finds effect sources that are close enough to the space to have an effect
	 * inside it. The tolerance in z-coordinate is one millimeter so that
	 * components placed directly on top of the space or under the space are
	 * included but components on other floors are not.
	 *
	 * @param spaceEntity the space entity
	 *
	 * @return the effect sources
	 */
	Collection<Component> getEffectSourcesNear(Component spaceEntity) {
		double tolerance = rpEffectRange.getValue();

		ComponentFilter nearbyEffectSourcesFilter = AABBIntersectionFilter.ofComponentBounds(spaceEntity, tolerance,
			ONE_MILLIMETER_IN_METERS).and(rpEffectSources.getValue());

		return SMC.getModel().getComponents(nearbyEffectSourcesFilter);
	}

	EffectiveCoverageChecking getCheck(Component spaceEntity, Collection<Component> effectSources) {
		switch (EffectiveCoverageAreaBehaviour.fromPropertyKey(rpOcclusionAndBounds.getValue())) {
		case UNOCCLUDED:
			return new UnoccludedChecking(
				spaceEntity,
				effectSources,
				rpEffectRange.getValue(),
				rpMinimumCoverage.getValue());
		case UNOCCLUDED_WITHIN_AREA:
			return new UnoccludedWithinAreaChecking(
				spaceEntity,
				effectSources,
				rpEffectRange.getValue(),
				rpMinimumCoverage.getValue());
		case DISTANCE_OF_TRAVEL_WITHIN_AREA:
			return new DistanceOfTravelChecking(
				spaceEntity,
				effectSources,
				rpEffectRange.getValue(),
				rpMinimumCoverage.getValue());
		case OCCLUDED_WITHIN_AREA:
			return new OccludedWithinAreaChecking(
				spaceEntity,
				effectSources,
				rpEffectRange.getValue(),
				rpMinimumCoverage.getValue());
		default:
			throw new IllegalArgumentException(
				"Invalid value for effective coverage rule \"Occlusion and Bounds\"-behaviour:"
					+ rpOcclusionAndBounds.getValue());
		}
	}

	Result createResult(CoverageAreaViolation violation, ResultFactory resultFactory) {
		DecimalFormat df = new DecimalFormat(DECIMAL_FORMAT_VALUE);
		df.setRoundingMode(RoundingMode.FLOOR);

		return resultFactory
			.create(
				resources.getString("Result.EffectiveCoverageAreaViolation.Name"),
				resources.getString("Result.EffectiveCoverageAreaViolation.Description",
					violation.spaceEntity.getName(),
					violation.effectSources.stream()
						.map(Component::getName)
						.collect(Collectors.joining(", ")),
					df.format(violation.coverageRatio * 100),
					df.format(violation.minimumCoverage * 100)))
			.withCustomUniqueKey(violation.spaceEntity.getGUID())
			.withInvolvedComponents(violation.effectSources)
			.withVisualization(visualization -> {
				visualization
					.addComponentsWithColor(violation.effectSources, EffectiveAreaVisualizationColor.SOURCE_RED);
				visualization.addComponentWithColor(violation.spaceEntity,
					EffectiveAreaVisualizationColor.SPACE_GREEN_TRANSPARENT);

				// small 1mm increase to visualization elevation to avoid Z-fighting
				double elevation = violation.spaceEntity.getBoundingBox().getLowerBound().getZ()
					+ ONE_MILLIMETER_IN_METERS;
				VisualizationItem coverageAreaVisualization = VisualizationItem.createArea(
					violation.coverage, elevation)
					.withColor(EffectiveAreaVisualizationColor.COVERAGE_AREA_RED_TRANSPARENT);

				visualization.addVisualizationItem(coverageAreaVisualization);
			});
	}

	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}
}
