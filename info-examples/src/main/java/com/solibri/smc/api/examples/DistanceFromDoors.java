package com.solibri.smc.api.examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.primitive2d.MPolygon2d;
import com.solibri.geometry.primitive2d.Polygon2d;
import com.solibri.smc.api.footprints.Footprint;
import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.model.components.Door;
import com.solibri.smc.api.model.components.Space;

/**
 * This example custom Information fetches the maximum distance of any place inside a space to a door. The information
 * is defined only for {@link Space}s that have a {@link Door}.
 */
public class DistanceFromDoors implements Information<Double> {

	@Override
	public String getUniqueId() {
		return "Largest distance from a door";
	}

	@Override
	public Optional<Double> getInformation(Component component) {
		// This is not defined for non-spaces.
		if (!(component instanceof Space)) {
			return Optional.empty();
		}
		Space space = (Space) component;
		Collection<Door> doors = space.getDoors();

		// This is not defined for spaces without doors.
		if (doors.isEmpty()) {
			return Optional.empty();
		}

		Footprint footprint = space.getFootprint();
		Polygon2d outline = footprint.getOutline();

		/*
		 * To calculate the distance, take all the corner points as candidates, but also take the the center point as a
		 * candidate if it is inside the space. For some spaces, such as an L-shaped space, the center point might not
		 * be inside the space. This is an attempt to be "smart" and thus the algorithm may give false results for some
		 * kind of spaces. One fix for this would be to investigate more points.
		 *
		 */
		List<Vector2d> allPoints = new ArrayList<>(outline.getVertices());
		Vector2d center = outline.getCentroid();
		if (outline.contains(center)) {
			allPoints.add(center);
		}

		/*
		 * For each point find the shortest distance for any door polyogn. Then from these distances choose the largest.
		 */
		List<MPolygon2d> doorPolygons = doors.stream().map(Door::getFootprint).map(Footprint::getArea).flatMap(area -> area.getPolygons().stream())
			.collect(Collectors.toList());
		double maxDistanceOfAllMinDistances = Double.MIN_VALUE;
		for (Vector2d point : allPoints) {
			double minDistanceForGivenPoint = Double.MAX_VALUE;
			for (MPolygon2d doorPolygon : doorPolygons) {
				double distance = doorPolygon.distance(point);
				if (distance < minDistanceForGivenPoint) {
					minDistanceForGivenPoint = distance;
				}
			}
			if (minDistanceForGivenPoint > maxDistanceOfAllMinDistances) {
				maxDistanceOfAllMinDistances = minDistanceForGivenPoint;
			}
		}

		return Optional.of(maxDistanceOfAllMinDistances);
	}

	@Override
	public PropertyType getType() {
		return PropertyType.LENGTH;
	}

}
