package com.solibri.smc.api.examples.spaceconnectionrule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.components.Door;
import com.solibri.smc.api.model.components.Opening;
import com.solibri.smc.api.model.components.Space;

/**
 * This class is used to check for Space (A) to Space (B) direct access
 * connection.
 */
class SpaceToSpaceConnectionChecking {

	private static final double NEARBY_SPACES_CONTRACTION_TOLERANCE_M = -0.05; // -5cm
	private static final double NEARBY_SPACES_EXPANSION_TOLERANCE_M = 1.0; // 1m

	private final SpaceConnectionRule spaceConnectionRule;
	private final RuleResources resources;

	/**
	 * Constructor.
	 *
	 * @param spaceConnectionRule the space connection rule
	 */
	SpaceToSpaceConnectionChecking(SpaceConnectionRule spaceConnectionRule) {
		this.spaceConnectionRule = spaceConnectionRule;
		this.resources = RuleResources.of(spaceConnectionRule);
	}

	/**
	 * Checks for direct space (A) to space (B) connection.
	 *
	 * @param spaceA the Space A
	 * @param resultFactory the result factory
	 */
	Collection<Result> checkDirectSpaceToSpaceConnection(Space spaceA, ResultFactory resultFactory) {
		Collection<Result> results = new ArrayList<>();

		Set<Space> bSpaces = findNearBySpaces(spaceA);

		Set<Space> bSpacesWithCommonDoors = getSpacesWithCommonDoors(spaceA, bSpaces);
		Set<Space> bSpacesWithoutCommonDoors = getSpacesWithoutCommonDoors(spaceA, bSpaces);
		Set<Space> bSpacesWithCommonOpenings = getSpacesWithCommonOpenings(spaceA, bSpaces);
		Set<Space> bSpacesWithoutCommonOpenings = getSpacesWithoutCommonOpenings(spaceA, bSpaces);
		// B spaces without any connection belong to both bSpacesWithoutCommonDoors and bSpacesWithoutCommonOpenings
		Set<Space> bSpacesWithoutAnyConnection = bSpacesWithoutCommonDoors.stream()
			.filter(space -> new HashSet<>(bSpacesWithoutCommonOpenings)
				.contains(space))
			.collect(Collectors.toSet());
		// B spaces with any connection belong to either bSpacesWithCommonDoors
		// or bSpacesWithCommonOpenings
		Set<Space> bSpacesWithAnyConnection = new HashSet<>();
		bSpacesWithAnyConnection.addAll(bSpacesWithCommonDoors);
		bSpacesWithAnyConnection.addAll(bSpacesWithCommonOpenings);

		String typeOfAccessCondition = spaceConnectionRule.rpTypeOfAccessCondition.getValue();
		String directAccessCondition = spaceConnectionRule.rpDirectAccessCondition.getValue();
		if (SpaceConnectionRule.DIRECT_ACCESS_REQUIRED.equals(directAccessCondition)) {
			if (SpaceConnectionRule.TYPE_ANY_DOOR_OR_OPENING.equals(typeOfAccessCondition)) {
				for (Space spaceB : bSpacesWithoutAnyConnection) {
					results.add(createRequiredDirectAccessResult(spaceA, spaceB, resultFactory));
				}

			} else if (SpaceConnectionRule.TYPE_CONSIDER_DOORS.equals(typeOfAccessCondition)) {
				for (Space spaceB : bSpacesWithoutCommonDoors) {
					results.add(createRequiredDirectAccessResult(spaceA, spaceB, resultFactory));
				}

			} else if (SpaceConnectionRule.TYPE_CONSIDER_OPENINGS.equals(typeOfAccessCondition)) {
				for (Space spaceB : bSpacesWithoutCommonOpenings) {
					results.add(createRequiredDirectAccessResult(spaceA, spaceB, resultFactory));
				}
			}

		} else if (SpaceConnectionRule.DIRECT_ACCESS_FORBIDDEN.equals(directAccessCondition)) {
			if (SpaceConnectionRule.TYPE_ANY_DOOR_OR_OPENING.equals(typeOfAccessCondition)) {
				for (Space spaceB : bSpacesWithAnyConnection) {
					Set<Component> connectingEntities = new HashSet<>();
					connectingEntities.addAll(
						spaceB.getDoors().stream().filter(door -> spaceA.getDoors().contains(door))
							.collect(Collectors.toSet()));
					connectingEntities
						.addAll(
							spaceB.getOpenings().stream().filter(opening -> spaceA.getOpenings().contains(opening))
								.collect(Collectors.toSet()));
					results.add(createForbiddenDirectAccessResult(spaceA, spaceB, connectingEntities, resultFactory));
				}

			} else if (SpaceConnectionRule.TYPE_CONSIDER_DOORS.equals(typeOfAccessCondition)) {
				for (Space spaceB : bSpacesWithCommonDoors) {
					Set<Component> connectingEntities = new HashSet<>();
					connectingEntities.addAll(
						spaceB.getDoors().stream().filter(door -> spaceA.getDoors().contains(door))
							.collect(Collectors.toSet()));
					results.add(createForbiddenDirectAccessResult(spaceA, spaceB, connectingEntities, resultFactory));
				}

			} else if (SpaceConnectionRule.TYPE_CONSIDER_OPENINGS.equals(typeOfAccessCondition)) {
				for (Space spaceB : bSpacesWithCommonOpenings) {
					Set<Component> connectingEntities = new HashSet<>();
					connectingEntities
						.addAll(
							spaceB.getOpenings().stream().filter(opening -> spaceA.getOpenings().contains(opening))
								.collect(Collectors.toSet()));
					results.add(createForbiddenDirectAccessResult(spaceA, spaceB, connectingEntities, resultFactory));
				}
			}
		} else if (SpaceConnectionRule.DIRECT_ACCESS_ALLOWED.equals(typeOfAccessCondition)) {
			if (SpaceConnectionRule.TYPE_ANY_DOOR_OR_OPENING.equals(typeOfAccessCondition)) {
				// This branch should never bring up any issues.
				return Collections.emptyList();
			} else if (SpaceConnectionRule.TYPE_CONSIDER_DOORS.equals(typeOfAccessCondition)) {
				/*
				 * If the space B has connections, at least one of them must be
				 * a door. So we can just check all the spaces with opening
				 * connections, and raise an issue if it does not have also a
				 * door connection.
				 */
				for (Space spaceB : bSpacesWithCommonOpenings) {
					Set<Component> connectingDoorEntities = spaceB.getDoors().stream()
						.filter(door -> spaceA.getDoors().contains(door))
						.collect(Collectors.toSet());
					if (connectingDoorEntities.isEmpty()) {
						Set<Component> connectingOpeningEntities =
							spaceB.getOpenings().stream()
								.filter(opening -> spaceA.getOpenings().contains(opening))
								.collect(Collectors.toSet());
						results.add(
							createTypeOfDirectAccessResult(
								spaceA,
								spaceB,
								connectingOpeningEntities,
								resultFactory));
					}
				}

			} else if (SpaceConnectionRule.TYPE_CONSIDER_OPENINGS.equals(typeOfAccessCondition)) {
				/*
				 * If the space B has connections, at least one of them must be
				 * an opening. So we can just check all the spaces with door
				 * connections, and raise an issue if it does not have also an
				 * opening connection.
				 */
				for (Space spaceB : bSpacesWithCommonOpenings) {
					Set<Component> connectingOpeningEntities =
						spaceB.getOpenings().stream()
							.filter(opening -> spaceA.getOpenings().contains(opening))
							.collect(Collectors.toSet());
					if (connectingOpeningEntities.isEmpty()) {
						Set<Component> connectingDoorEntities =
							spaceB.getDoors().stream()
								.filter(door -> spaceA.getDoors().contains(door))
								.collect(Collectors.toSet());
						results.add(createTypeOfDirectAccessResult(
							spaceA,
							spaceB,
							connectingDoorEntities,
							resultFactory));
					}
				}
			}
		}
		return results;
	}

	private Set<Space> findNearBySpaces(Space spaceA) {
		ComponentFilter potentialBSpacesFilter = AABBIntersectionFilter
			.ofComponentBounds(spaceA, NEARBY_SPACES_EXPANSION_TOLERANCE_M, NEARBY_SPACES_CONTRACTION_TOLERANCE_M)
			.and(spaceConnectionRule.rpSpacesFilterB.getValue())
			.and(component -> !component.equals(spaceA));

		return new HashSet<>(SMC.getModel().getComponents(potentialBSpacesFilter, Space.class));
	}

	/**
	 * Create result when there is required direct access from Space A to Space
	 * B but it does not exist.
	 *
	 * @param spaceA the space A
	 * @param offendingSpace the offending space b
	 * @param resultFactory the result manager
	 */
	private Result createRequiredDirectAccessResult(Space spaceA, Space offendingSpace, ResultFactory resultFactory) {
		String spaceADisplayName = spaceA.getName();

		String name = resources.getString("Result.NoDirectAccessFromAtoB.Name");
		String description = resources.getString("Result.NoDirectAccessFromAtoB.Description", spaceADisplayName);
		return resultFactory
			.create(name, description)
			.withCustomUniqueKey("createRequiredDirectAccessResult" + spaceA.getGUID() + ":" + offendingSpace
				.getGUID())
			.withInvolvedComponent(offendingSpace);
	}

	/**
	 * Create result when direct access from Space A to Space B exists and it is
	 * forbidden.
	 *
	 * @param spaceA the space A
	 * @param spaceB the space info for B
	 * @param connectionEntities the set of the forbidden connecting entities
	 * @param resultFactory the result factory
	 */
	private Result createForbiddenDirectAccessResult(Space spaceA,
													 Space spaceB,
													 Set<Component> connectionEntities,
													 ResultFactory resultFactory) {

		String spaceADisplayName = spaceA.getName();
		String spaceBDisplayName = spaceB.getName();
		String name = resources.getString("Result.NoDirectAccessAllowed.Name");

		String description = resources.getString("Result.NoDirectAccessAllowed.Description", spaceADisplayName,
			spaceBDisplayName);
		return resultFactory
			.create(name, description)
			.withCustomUniqueKey(
				"createForbiddenDirectAccessResult" + spaceA.getGUID() + ":" + spaceB
					.getGUID())
			.withInvolvedComponent(spaceB)
			.withInvolvedComponents(connectionEntities);
	}

	/**
	 * Create the result based on user input. If the direct access exists and
	 * has no door then create a DirectAccessWithoutDoor issue. If the direct
	 * access exists and has no opening then create a DirectAccessWithoutOpening
	 * issue.
	 *
	 * @param spaceA the space A
	 * @param spaceB the space B
	 * @param conflictingEntities the conflicting connections
	 * @param resultFactory the result factory
	 */
	private Result createTypeOfDirectAccessResult(
		Space spaceA,
		Space spaceB,
		Set<Component> conflictingEntities,
		ResultFactory resultFactory) {
		String spaceADisplayName = spaceA.getName();
		String name = "";
		String descriptionType = "";

		if (SpaceConnectionRule.TYPE_CONSIDER_DOORS.equals(spaceConnectionRule.rpTypeOfAccessCondition.getValue())) {
			name = resources.getString("Result.DirectAccessWithoutDoor.Name");
			descriptionType = "Result.DirectAccessWithoutDoor.Description";
		} else if (SpaceConnectionRule.TYPE_CONSIDER_OPENINGS
			.equals(spaceConnectionRule.rpTypeOfAccessCondition.getValue())) {
			name = resources.getString("Result.DirectAccessWithoutOpening.Name");
			descriptionType = "Result.DirectAccessWithoutOpening.Description";
		}

		List<String> spacesWithoutConnectionNames = new ArrayList<>();
		spacesWithoutConnectionNames.add(spaceB.getName());

		String description = resources.getString(descriptionType, spaceADisplayName, spacesWithoutConnectionNames);

		return resultFactory
			.create(name, description)
			.withCustomUniqueKey("createTypeOfDirectAccessResult" + spaceA.getGUID() + ":" + spaceB.getGUID())
			.withInvolvedComponent(spaceB)
			.withInvolvedComponents(conflictingEntities);
	}

	/**
	 * Fetches all the spaces b that are connected to a with a door.
	 *
	 * @param aSpace the space a
	 * @param bSpaces the spaces b
	 *
	 * @return a set of b spaces
	 */
	private Set<Space> getSpacesWithCommonDoors(Space aSpace, Collection<Space> bSpaces) {
		Set<Door> aDoors = new HashSet<>(aSpace.getDoors());
		return bSpaces.stream()
			.filter(space -> space.getDoors().stream().anyMatch(door -> aDoors.contains(door)))
			.collect(Collectors.toSet());
	}

	/**
	 * Fetches all the spaces b that are not connected to a with a door.
	 *
	 * @param aSpace the space a
	 * @param bSpaces the spaces b
	 *
	 * @return a set of b spaces
	 */
	private Set<Space> getSpacesWithoutCommonDoors(Space aSpace, Collection<Space> bSpaces) {
		Set<Door> aDoors = new HashSet<>(aSpace.getDoors());
		// Fetches all the spaces b that are not connected to a with a door.
		return bSpaces.stream()
			.filter(space -> space.getDoors().stream().noneMatch(door -> aDoors.contains(door)))
			.collect(Collectors.toSet());
	}

	/**
	 * Fetches all the spaces b that are connected to a with an opening.
	 *
	 * @param aSpace the space a
	 * @param bSpaces the spaces b
	 *
	 * @return a set of b spaces
	 */
	private Set<Space> getSpacesWithCommonOpenings(Space aSpace, Collection<Space> bSpaces) {
		Set<Opening> aOpenings = new HashSet<>(aSpace.getOpenings());
		// Fetches all the spaces b that are connected to a with an opening.
		return bSpaces.stream()
			.filter(space -> space.getOpenings().stream().anyMatch(opening -> aOpenings.contains(opening)))
			.collect(Collectors.toSet());
	}

	/**
	 * Fetches all the spaces b that are not connected to a with an opening.
	 *
	 * @param aSpace the space a
	 * @param bSpaces the spaces b
	 *
	 * @return a set of b spaces
	 */
	private Set<Space> getSpacesWithoutCommonOpenings(Space aSpace, Collection<Space> bSpaces) {
		Set<Opening> aOpenings = new HashSet<>(aSpace.getOpenings());
		// Fetches all the spaces b that are not connected to a with an opening.
		return bSpaces.stream()
			.filter(space -> space.getOpenings().stream().noneMatch(opening -> aOpenings.contains(opening)))
			.collect(Collectors.toSet());
	}
}
