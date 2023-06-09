package com.solibri.smc.api.examples.parkingrule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.solibri.geometry.linearalgebra.MVector2d;
import com.solibri.geometry.linearalgebra.MVector3d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.linearalgebra.Vector3d;
import com.solibri.geometry.primitive2d.Area;
import com.solibri.geometry.primitive2d.MArea;
import com.solibri.geometry.primitive2d.MPolygon2d;
import com.solibri.geometry.primitive2d.MSegment2d;
import com.solibri.geometry.primitive2d.Rectangle2d;
import com.solibri.geometry.primitive2d.Segment2d;
import com.solibri.geometry.primitive3d.AABB3d;
import com.solibri.geometry.primitive3d.Rectangle3d;
import com.solibri.geometry.primitive3d.Segment3d;
import com.solibri.smc.api.SMC;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;

final class OrientationChecking {

	private static final double ANGLE_TOLERANCE = Math.toRadians(2.0);

	private static final double HALF_PI = 0.5 * Math.PI;

	private static final double AISLE_COMBINE_TOLERANCE = 0.300;

	private static final double RECTANGLE_TOLERANCE = 0.007;

	private static final double SEARCH_DISTANCE = 5.0;

	private static final double INTERSECTION_TOLERANCE = 0.5;

	private static final double ELEVATION_TOLERANCE = 0.300;

	static final class Intersection {

		final MVector3d point = MVector3d.create();

		double distance = Double.MAX_VALUE;

		double angle = HALF_PI;
	}

	enum Orientation {
		Parallel, Perpendicular, Angled, Undefined
	}

	private final ComponentFilter parkingObstructionsFilter;

	OrientationChecking(ComponentFilter parkingOrientationFilter) {
		this.parkingObstructionsFilter = parkingOrientationFilter;
	}

	/*
	 * Finds the orientation of a parking space by creating a perpendicular
	 * segment from each rectangle edge and see if it intersects with a parking
	 * aisle. Based on these intersections we find the orientation of the
	 * parking space. If no intersection was found we check if the shape of the
	 * parking space is not a rectangle, meaning an angled parking space.
	 */
	Orientation findOrientation(ParkingSpace parkingSpace) {

		// Combine the areas of all aisles and get the polygons representation
		ComponentFilter obstructionsFilter = AABBIntersectionFilter
			.ofComponentBounds(parkingSpace.getEntity(), SEARCH_DISTANCE, ELEVATION_TOLERANCE)
			.and(parkingObstructionsFilter);

		Collection<Component> closeComponents = SMC.getModel().getComponents(obstructionsFilter);

		// Retrieve the aisle with the same elevation
		List<Component> closeAislesEntities = getAisleEntitiesWithSameElevation(closeComponents,
			parkingSpace.getElevation());
		Area aislesArea = getCombinedAislesArea(closeAislesEntities);

		List<MPolygon2d> aislesPolygons = aislesArea.getPolygons();

		// Find the short and long edges of the rectangle
		Rectangle3d rectangle = parkingSpace.getRectangle();
		List<Segment3d> shortSegments = rectangle.getShorterRectangleSegments();
		List<Segment3d> longSegments = rectangle.getLongerRectangleSegments();

		// Check if perpendicular by passing the pair of shorter segments
		Intersection intersection = getClosestIntersection(shortSegments, aislesPolygons);
		if (intersection.distance < INTERSECTION_TOLERANCE && isRightAngle(intersection.angle)) {
			return Orientation.Perpendicular;
		}

		// Check if parallel by passing the pair of longer segments
		intersection = getClosestIntersection(longSegments, aislesPolygons);
		if (intersection.distance < INTERSECTION_TOLERANCE && isRightAngle(intersection.angle)) {
			return Orientation.Parallel;
		}

		if (isAngled(parkingSpace, aislesArea)) {
			return Orientation.Angled;
		}

		return Orientation.Undefined;
	}

	/*
	 * This methods checks if the elevation of the entity is the same as the
	 * parking space. In that case the entity is added to the list of parking
	 * aisles.
	 */
	private List<Component> getAisleEntitiesWithSameElevation(final Collection<Component> closeComponents,
															  double elevation) {
		List<Component> closeAislesEntities = new ArrayList<>();
		for (Component entity : closeComponents) {
			AABB3d aabb = entity.getBoundingBox();
			if (aabb != null) {
				double bottomElevation = aabb.getLowerBound().getZ();
				if (Math.abs(bottomElevation - elevation) < ELEVATION_TOLERANCE) {
					closeAislesEntities.add(entity);
				}
			}
		}

		return closeAislesEntities;
	}

	private Area getCombinedAislesArea(List<Component> aisleEntities) {

		if (aisleEntities.size() == 0) {
			return MArea.create();
		}

		if (aisleEntities.size() == 1) {
			Component entity = aisleEntities.get(0);
			Area area = MArea.create(entity.getFootprint().getArea());
			return area;

		} else {
			ArrayList<MArea> areas = new ArrayList<>();
			for (Component entity : aisleEntities) {
				MArea area = MArea.create(entity.getFootprint().getArea());
				area.resize(AISLE_COMBINE_TOLERANCE);
				areas.add(area);
			}

			MArea area = MArea.create(areas);
			area.resize(-AISLE_COMBINE_TOLERANCE);
			return area;
		}
	}

	/*
	 * This method finds the closest intersection point between the segment pair
	 * and the aisles polygons. Distance and angle of the intersection are
	 * calculated as well.
	 */
	private static Intersection getClosestIntersection(final List<Segment3d> segmentPair,
													   List<MPolygon2d> aislePolygons) {
		Vector3d[] searchSegment1 = getPerpendicularSearchSegment(segmentPair.get(0), SEARCH_DISTANCE);
		Vector3d[] searchSegment2 = getPerpendicularSearchSegment(segmentPair.get(1), SEARCH_DISTANCE);

		Intersection intersection1 = findClosestIntersection(searchSegment1[0], searchSegment1[1], aislePolygons);
		Intersection intersection2 = findClosestIntersection(searchSegment2[0], searchSegment2[1], aislePolygons);

		if (intersection1.distance < intersection2.distance) {
			return intersection1;
		}

		return intersection2;
	}

	static Vector3d[] getPerpendicularSearchSegment(Segment3d segment, double length) {
		/*
		 * The first point of our perpendicular segment will be the center point
		 * of startPoint and endPoint.
		 */
		MVector3d firstPoint = MVector3d.create();
		firstPoint.set(segment.getStartPoint());
		firstPoint.interpolateInPlace(segment.getEndPoint(), 0.5);

		// Calculate the segment's direction
		MVector3d direction = MVector3d.create(segment.getEndPoint());
		direction.subtractInPlace(segment.getStartPoint());
		direction.normalizeInPlace();

		direction.crossProductInPlace(Vector3d.UNIT_Z);

		// Calculate the second point of the perpendicular vector
		MVector3d secondPoint = direction.scale(length);
		secondPoint.addInPlace(firstPoint);

		return new Vector3d[] {
			firstPoint,
			secondPoint};
	}

	static Intersection findClosestIntersection(Vector3d segmentStart, Vector3d segmentEnd,
												final List<MPolygon2d> polygons) {
		Intersection intersection = new Intersection();

		Segment2d segment = MSegment2d.create(segmentStart.to2dVector(), segmentEnd.to2dVector());
		Vector2d direction = segment.getEndPoint().subtract(segment.getStartPoint());

		Optional<MVector2d> result = Optional.empty();
		MVector2d polygonEdge = MVector2d.create();
		MSegment2d polygonSegment = MSegment2d.create(MVector2d.create(), MVector2d.create());

		MVector2d current = MVector2d.create();
		MVector2d next = MVector2d.create();

		for (MPolygon2d polygon : polygons) {
			List<Vector2d> vertices2d = polygon.getVertices();
			for (int i = 0; i < vertices2d.size(); i++) {
				int j = (i + 1) % vertices2d.size();
				current.set(vertices2d.get(i));
				next.set(vertices2d.get(j));

				polygonSegment.set(current, next);
				result = segment.intersect(polygonSegment);
				if (result.isPresent()) {
					polygonEdge.set(current);
					polygonEdge.subtractInPlace(next);

					MVector2d resultPoint = result.get();
					double distance = resultPoint.distance(segment.getStartPoint());
					if (distance < intersection.distance) {
						intersection.point.set(resultPoint.to3dVector());
						intersection.distance = distance;
						intersection.angle = direction.angle(polygonEdge);
					}
				}
			}
		}

		return intersection;
	}

	/*
	 * Returns true if the shape of the parking space is not rectangular and is
	 * close enough to the parking aisle.
	 */
	private static boolean isAngled(final ParkingSpace parkingSpace, final Area aislesArea) {
		Area parkingSpaceArea = MArea.create(Arrays.asList(parkingSpace.getFootprint()));
		if (!isRectangle(parkingSpaceArea)) {
			MArea intersect = MArea.create(parkingSpaceArea);
			intersect.resize(INTERSECTION_TOLERANCE);
			intersect.intersect(aislesArea);

			return !intersect.isEmpty();
		}

		return false;
	}

	static boolean isRectangle(Area area) {
		try {
			Rectangle2d rect = area.getMinimumBoundingRectangle();

			double rectangleArea = rect.getArea();
			double areaValue = area.getSize();

			/*
			 * Check that the rectangle area and the area calculated from the Area
			 * object are the same ( 0.7% tolerance ).
			 */
			return Math.abs(rectangleArea - areaValue) / areaValue < RECTANGLE_TOLERANCE;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	static boolean isRightAngle(double angle) {
		return Math.abs(angle - HALF_PI) < ANGLE_TOLERANCE;
	}
}
