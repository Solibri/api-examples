package com.solibri.smc.api.examples.spaceconnectionrule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.Property;
import com.solibri.smc.api.model.PropertySet;
import com.solibri.smc.api.model.components.Door;
import com.solibri.smc.api.model.components.Opening;
import com.solibri.smc.api.model.components.Space;
import com.solibri.smc.api.model.components.Wall;

/**
 * This class is used to check for direct access from Space (A) to outside.
 */
class SpaceToOutsideChecking {

	private static final double CLOSE_WALLS_CONTRACTION_TOLERANCE_M = -0.05;

	private final SpaceConnectionRule spaceConnectionRule;

	private final Set<Component> externalWalls;

	private final RuleResources resources;

	/**
	 * Constructor.
	 *
	 * @param spaceConnectionRule the space connection rule
	 */
	SpaceToOutsideChecking(SpaceConnectionRule spaceConnectionRule) {
		this.spaceConnectionRule = spaceConnectionRule;
		this.resources = RuleResources.of(spaceConnectionRule);
		this.externalWalls = new HashSet<>();
	}

	/**
	 * Checks for direct access from Space A to outside.
	 *
	 * @param spaceA the space A
	 *
	 * @return the checking results
	 */
	Collection<Result> checkSpaceToOutsideAccess(Space spaceA, ResultFactory resultFactory) {
		Collection<Result> results = new ArrayList<>();

		/*
		 * Retrieve all the doors or openings located in an external wall or
		 * next to an external wall.
		 */
		Set<Door> doorsToOutside = new HashSet<>();
		Set<Opening> openingsToOutside = new HashSet<>();
		for (Door door : spaceA.getDoors()) {
			if (isConnectionToOutside(door)) {
				doorsToOutside.add(door);
			}
		}
		for (Opening opening : spaceA.getOpenings()) {
			if (isConnectionToOutside(opening)) {
				openingsToOutside.add(opening);
			}
		}

		String directAccessOutsideCondition = spaceConnectionRule.rpDirectAccessOutsideCondition.getValue();
		String typeOfAccessOutsideCondition = spaceConnectionRule.rpTypeOfAccessOutsideCondition.getValue();
		if (SpaceConnectionRule.EXIT_ALLOWED.equals(directAccessOutsideCondition)) {
			if (SpaceConnectionRule.TYPE_ANY_DOOR_OR_OPENING.equals(typeOfAccessOutsideCondition)) {
				return Collections.emptyList();
			} else if (SpaceConnectionRule.TYPE_CONSIDER_DOORS.equals(typeOfAccessOutsideCondition)) {
				// If access to outside is optional, but the type must be a
				// door, we raise an issue if the only type of access is
				// openings.
				if (doorsToOutside.isEmpty() && !openingsToOutside.isEmpty()) {
					for (Opening opening : openingsToOutside) {
						results.add(
							createForbiddenTypeOfAccessToOutsideResult(spaceA, opening,
								"DirectAccessToOutsideByForbiddenOpening", resultFactory));
					}
				}

			} else if (SpaceConnectionRule.TYPE_CONSIDER_OPENINGS.equals(typeOfAccessOutsideCondition)) {
				// If access to outside is optional, but the type must be a
				// door, we raise an issue if the only type of access is
				// openings.
				if (!doorsToOutside.isEmpty() && openingsToOutside.isEmpty()) {
					for (Door door : doorsToOutside) {
						results.add(createForbiddenTypeOfAccessToOutsideResult(spaceA, door,
							"DirectAccessToOutsideByForbiddenDoor", resultFactory));
					}
				}
			}
			return results;
		} else if (SpaceConnectionRule.EXIT_REQUIRED.equals(directAccessOutsideCondition)) {
			if (SpaceConnectionRule.TYPE_ANY_DOOR_OR_OPENING.equals(typeOfAccessOutsideCondition)) {
				if (doorsToOutside.isEmpty() && openingsToOutside.isEmpty()) {
					results.add(createRequiredAccessToOutsideResult(spaceA, resultFactory));
				}
			} else if (SpaceConnectionRule.TYPE_CONSIDER_DOORS.equals(typeOfAccessOutsideCondition)) {
				if (doorsToOutside.isEmpty()) {
					results.add(createRequiredAccessToOutsideResult(spaceA, resultFactory));
				}
			} else if (SpaceConnectionRule.TYPE_CONSIDER_OPENINGS.equals(typeOfAccessOutsideCondition)) {
				if (openingsToOutside.isEmpty()) {
					results.add(createRequiredAccessToOutsideResult(spaceA, resultFactory));
				}
			}
		} else if (SpaceConnectionRule.EXIT_FORBIDDEN.equals(directAccessOutsideCondition)) {
			if (SpaceConnectionRule.TYPE_ANY_DOOR_OR_OPENING.equals(typeOfAccessOutsideCondition)) {
				for (Door door : doorsToOutside) {
					results.add(createForbiddenAccessToOutsideResult(spaceA, door, resultFactory));
				}
				for (Opening opening : openingsToOutside) {
					results.add(createForbiddenAccessToOutsideResult(spaceA, opening, resultFactory));
				}
			} else if (SpaceConnectionRule.TYPE_CONSIDER_DOORS.equals(typeOfAccessOutsideCondition)) {
				for (Door door : doorsToOutside) {
					results.add(createForbiddenAccessToOutsideResult(spaceA, door, resultFactory));
				}
			} else if (SpaceConnectionRule.TYPE_CONSIDER_OPENINGS.equals(typeOfAccessOutsideCondition)) {
				for (Opening opening : openingsToOutside) {
					results.add(createForbiddenAccessToOutsideResult(spaceA, opening, resultFactory));
				}
			}
		}
		return results;
	}

	private Result createForbiddenTypeOfAccessToOutsideResult(Space spaceA, Component forbiddenTypeOfAccessComponent,
															  String resultTypeInKey, ResultFactory resultFactory) {

		final String displayName = spaceA.getName();
		final String resultPropertyKey = "Result." + resultTypeInKey;

		String name = resources.getString(resultPropertyKey + ".Name");
		String description = resources.getString(resultPropertyKey + ".Description", displayName);
		return resultFactory
			.create(name, description)
			.withCustomUniqueKey(
				"createForbiddenTypeOfAccessToOutsideResult" + spaceA.getGUID() + forbiddenTypeOfAccessComponent
					.getGUID())
			.withInvolvedComponents(externalWalls)
			.withInvolvedComponent(forbiddenTypeOfAccessComponent);
	}

	private boolean isConnectionToOutside(Component doorOrOpening) {
		ComponentFilter closeWallsFilter = AABBIntersectionFilter
			.ofComponentBounds(doorOrOpening, 0.0, CLOSE_WALLS_CONTRACTION_TOLERANCE_M);
		Collection<Wall> closeWalls = spaceConnectionRule.getTargetModel().getComponents(closeWallsFilter, Wall.class);

		/*
		 * Add all the walls to a set for the purpose of visualization.
		 */
		this.externalWalls.addAll(closeWalls);
		for (Wall wall : closeWalls) {
			for (PropertySet propertySet : wall.getPropertySets("Pset_WallCommon")) {
				Optional<Property<Boolean>> isExternalProperty = propertySet.getProperty("IsExternal");
				if (isExternalProperty.isPresent() && isExternalProperty.get().getValue().isPresent()
					&& isExternalProperty.get().getValue().get()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Create result when Space A does not have direct access to outside but it
	 * is required.
	 *
	 * @param spaceA the space A
	 * @param resultFactory the result factory
	 *
	 * @return the checking result
	 */
	private Result createRequiredAccessToOutsideResult(Space spaceA, ResultFactory resultFactory) {

		String displayName = spaceA.getName();

		String name = resources.getString("Result.NoDirectAccessToOutside.Name");
		String description = resources.getString("Result.NoDirectAccessToOutside.Description", displayName);
		return resultFactory
			.create(name, description)
			.withCustomUniqueKey("createRequiredAccessToOutsideResult" + spaceA.getGUID())
			.withInvolvedComponents(externalWalls)
			.withInvolvedComponents(spaceA.getDoors())
			.withInvolvedComponents(spaceA.getOpenings());
	}

	/**
	 * Create result when Space A has direct access to outside but it is
	 * forbidden.
	 *
	 * @param spaceA the space A
	 * @param conflictingEntity the conflicting entity
	 * @param resultFactory the result factory
	 *
	 * @return the checking result
	 */
	private Result createForbiddenAccessToOutsideResult(Space spaceA, Component conflictingEntity,
														ResultFactory resultFactory) {

		String displayName = spaceA.getName();

		String name = resources.getString("Result.DirectAccessToOutsideForbidden.Name");
		String description = resources.getString("Result.DirectAccessToOutsideForbidden.Description", displayName);
		return resultFactory
			.create(name, description)
			.withCustomUniqueKey(
				"createForbiddenAccessToOutsideResult" + spaceA.getGUID() + conflictingEntity.getGUID())
			.withInvolvedComponents(externalWalls)
			.withInvolvedComponent(conflictingEntity);
	}
}
