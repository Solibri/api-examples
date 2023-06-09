package com.solibri.smc.api.examples.parkingrule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.solibri.geometry.algorithms.ConvexHull;
import com.solibri.geometry.linearalgebra.MVector2d;
import com.solibri.geometry.linearalgebra.MVector3d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.primitive2d.MPolygon2d;
import com.solibri.geometry.primitive2d.MSegment2d;
import com.solibri.geometry.primitive2d.Polygon2d;
import com.solibri.geometry.primitive2d.Rectangle2d;
import com.solibri.geometry.primitive2d.Segment2d;
import com.solibri.geometry.primitive3d.MAABB3d;
import com.solibri.geometry.primitive3d.Segment3d;
import com.solibri.smc.api.SMC;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;

final class ObstructionChecking {

	enum ObstructionType {
		NONE, ONE, BOTH
	}

	private static final double FREE_ZONE_TOLERANCE = 2e-3;
	private static final double ELEVATION_TOLERANCE = 0.1;
	private static final double MIN_HEIGHT_TOLERANCE = 1.0;
	private static final double MIN_CORNER_TOLERANCE = 0.15;
	private static final double SIDE_OBSTRUCTION_DISTANCE = 1.5;
	private static final double END_OBSTRUCTION_DISTANCE = 1.0;

	private final ComponentFilter parkingObstructionsFilter;
	private final double minimumWidth;
	private ParkingSpace parkingSpace;
	private List<Component> potentialObstructions;

	private double endObstructionCornerTolerance;
	private double sideObstructionCornerTolerance;

	ObstructionChecking(double minimumWidth, ComponentFilter parkingObstructionsFilter) {
		this.minimumWidth = minimumWidth;
		this.parkingObstructionsFilter = parkingObstructionsFilter;
	}

	void clearObstructionEntities() {
		if (potentialObstructions != null) {
			potentialObstructions.clear();
		}
	}

	void findParkingSpaceObstructions(ParkingSpace parkingSpace) {
		this.parkingSpace = parkingSpace;

		double actualMinimumWidthDiffHalf = (this.parkingSpace.getWidth() - minimumWidth) / 2;
		double actualMinimumLengthDiffHalf = (this.parkingSpace.getWidth() - minimumWidth) / 2;

		/*
		 * Use more tolerance in corners if the parking space is larger than the
		 * minimum size requirement. For side obstructions tolerance depends on
		 * length and for end obstructions tolerance depends on width.
		 */
		this.sideObstructionCornerTolerance = Math.max(MIN_CORNER_TOLERANCE, actualMinimumLengthDiffHalf);
		this.endObstructionCornerTolerance = Math.max(MIN_CORNER_TOLERANCE, actualMinimumWidthDiffHalf);

		this.potentialObstructions = findPotentialObstructions();
	}

	List<Component> getPotentialObstructions() {
		return this.potentialObstructions;
	}

	boolean isObstructionInsideParkingSpace(Component obstruction) {
		// Return true only if entityFootprint is entirely inside parking space
		Polygon2d obstructionFootprint = ConvexHull.of(obstruction.getFootprint().getOutline());
		Polygon2d parkingSpaceFootprint = MPolygon2d
			.create(Vector2d.to2dVectors(parkingSpace.getRectangle().getVertices()));

		return parkingSpaceFootprint.contains(obstructionFootprint);
	}

	ObstructionType getSideObstructionType() {
		return getObstructionType(getSideObstructionAreas());
	}

	ObstructionType getEndObstructionType() {
		return getObstructionType(getEndObstructionAreas());
	}

	ObstructionType getFreeZoneObstructionType(double freeZoneLength) {
		return getObstructionType(getFreeZoneObstructionAreas(freeZoneLength));
	}

	private ObstructionType getObstructionType(List<Rectangle2d> obstructionAreas) {
		boolean sideOneObstructed = potentialObstructions.stream()
			.anyMatch(potentialObs -> isEntityObstruction(potentialObs, obstructionAreas.get(0)));
		boolean sideTwoObstructed = potentialObstructions.stream()
			.anyMatch(potentialObs -> isEntityObstruction(potentialObs, obstructionAreas.get(1)));

		if (sideOneObstructed && sideTwoObstructed) {
			return ObstructionType.BOTH;
		}

		if (sideOneObstructed || sideTwoObstructed) {
			return ObstructionType.ONE;
		}

		return ObstructionType.NONE;
	}

	private List<Rectangle2d> getEndObstructionAreas() {
		return getRectanglesOnSides(parkingSpace.getShorterSegment(), parkingSpace.getLongerSegment(),
			END_OBSTRUCTION_DISTANCE, endObstructionCornerTolerance);
	}

	private List<Rectangle2d> getSideObstructionAreas() {
		return getRectanglesOnSides(parkingSpace.getLongerSegment(), parkingSpace.getShorterSegment(),
			SIDE_OBSTRUCTION_DISTANCE,
			sideObstructionCornerTolerance);
	}

	private List<Rectangle2d> getFreeZoneObstructionAreas(double freeZoneLength) {

		/*
		 * Compute the corner tolerance so that the side areas are reduced to be
		 * the length of the free zone area
		 */
		Segment3d parkingSpaceSide = parkingSpace.getLongerSegment();
		double sideLength = parkingSpaceSide.getStartPoint().distance(parkingSpaceSide.getEndPoint());
		double freeZoneCornerTolerance = (sideLength - freeZoneLength) / 2;

		return getRectanglesOnSides(parkingSpace.getLongerSegment(), parkingSpace.getShorterSegment(),
			SIDE_OBSTRUCTION_DISTANCE, freeZoneCornerTolerance);
	}

	/*
	 * Given two segments that define a rectangle parking space, returns two
	 * rectangles such that one touches touchingSegment and the other touches
	 * the segment on the opposing side of touchingSegment. The returned
	 * rectangles extend outward from the given parking space by the amount of
	 * extensionLength. The size of the returned rectangles is reduced by
	 * cornerTolerance on both sides that are close to a corner of the parking
	 * space rectangle.
	 */
	private List<Rectangle2d> getRectanglesOnSides(Segment3d touchingSegmentIn3d, Segment3d inbetweenSegmentIn3d,
												   double extensionLength,
												   double cornerTolerance) {

		Segment2d touchingSegment = MSegment2d
			.create(touchingSegmentIn3d.getStartPoint().to2dVector(), touchingSegmentIn3d.getEndPoint().to2dVector());
		Segment2d inbetweenSegment = MSegment2d
			.create(inbetweenSegmentIn3d.getStartPoint().to2dVector(), inbetweenSegmentIn3d.getEndPoint().to2dVector());

		Vector2d intersectingEnd = getIntersectingEnd(touchingSegment, inbetweenSegment);
		MVector2d inbetweenSegmentDifference = MVector2d
			.create(inbetweenSegment.getStartPoint().equals(intersectingEnd)
				? inbetweenSegment.getEndPoint()
				: inbetweenSegment.getStartPoint());
		inbetweenSegmentDifference.subtractInPlace(intersectingEnd);

		MVector2d extensionVector = MVector2d.create(inbetweenSegmentDifference);
		extensionVector.normalizeInPlace();
		extensionVector.negateInPlace();
		extensionVector.scaleInPlace(extensionLength);

		// Decrease the size of the touchingSegment for tolerance
		Segment2d touchingSegmentWithTolerance = getReducedSegment(touchingSegment, cornerTolerance);

		/*
		 * Use one of the parking space segments to compute the rectangular
		 * obstruction area on the side of that segment.
		 */
		MVector2d[] endOneArea = new MVector2d[4];
		endOneArea[0] = MVector2d.create(touchingSegmentWithTolerance.getStartPoint());
		endOneArea[1] = MVector2d.create(touchingSegmentWithTolerance.getEndPoint());
		MVector2d extendedVertex0 = MVector2d.create(touchingSegmentWithTolerance.getStartPoint());
		extendedVertex0.addInPlace(extensionVector);
		MVector2d extendedVertex1 = MVector2d.create(touchingSegmentWithTolerance.getEndPoint());
		extendedVertex1.addInPlace(extensionVector);
		endOneArea[2] = extendedVertex0;
		endOneArea[3] = extendedVertex1;

		/*
		 * Get the rectangle on the other side by translating the rectangle
		 * defined by endOneArea.
		 */
		MVector2d endAreaTranslator = MVector2d.create(inbetweenSegmentDifference);
		extensionVector.negateInPlace();
		endAreaTranslator.addInPlace(extensionVector);

		MVector2d[] endTwoArea = {
			MVector2d.create(endOneArea[0]), MVector2d.create(endOneArea[1]), MVector2d.create(endOneArea[2]), MVector2d
			.create(endOneArea[3])};
		for (MVector2d vec : endTwoArea) {
			vec.addInPlace(endAreaTranslator);
		}

		List<Rectangle2d> endObstructionAreas = new ArrayList<>();

		/*
		 * Getting the oriented bounding rectangle is used just for making sure the vertices are
		 * ordered (internally rotating calipers is used).
		 */
		endObstructionAreas.add(Polygon2d.create(Arrays.asList(endOneArea)).getOrientedBoundingRectangle());
		endObstructionAreas.add(Polygon2d.create(Arrays.asList(endTwoArea)).getOrientedBoundingRectangle());

		return endObstructionAreas;
	}

	/*
	 * Returns a copy of segment that is reduced in length from both ends by the
	 * amount of endReductionLength.
	 */
	private Segment2d getReducedSegment(Segment2d segment, double endReductionLength) {
		double sideLength = segment.getStartPoint().distance(segment.getEndPoint());
		double freeZoneLength = sideLength - endReductionLength + FREE_ZONE_TOLERANCE;

		MVector2d scaledTouchingSegmentStartingPoint = MVector2d.create(segment.getStartPoint());
		MVector2d scaledTouchingSegmentEndingPoint = MVector2d.create(segment.getEndPoint());

		double interpolationCoeff = 1.0 - (freeZoneLength / sideLength);
		scaledTouchingSegmentStartingPoint.interpolateInPlace(segment.getEndPoint(), interpolationCoeff);
		scaledTouchingSegmentEndingPoint.interpolateInPlace(segment.getStartPoint(), interpolationCoeff);

		return MSegment2d.create(scaledTouchingSegmentStartingPoint, scaledTouchingSegmentEndingPoint);
	}

	/*
	 * Returns the intersecting end of the two segments. Segments are assumed to
	 * have an intersecting end.
	 */
	private Vector2d getIntersectingEnd(Segment2d segment1, Segment2d segment2) {
		if (segment1.getStartPoint().equals(segment2.getStartPoint()) || segment1.getStartPoint()
			.equals(segment2.getEndPoint())) {
			return segment1.getStartPoint();
		}

		return segment1.getEndPoint();
	}

	/*
	 * Returns true if the footprint of potentialObstruction is in
	 * obstructionArea.
	 */
	private boolean isEntityObstruction(Component potentialObstruction, Rectangle2d obstructionArea) {
		Polygon2d potentialObstructionFootprint = ConvexHull.of(potentialObstruction.getFootprint().getOutline());
		Polygon2d obstructionAreaPolygon = obstructionArea;

		return potentialObstructionFootprint.intersects(obstructionAreaPolygon);
	}

	/*
	 * Returns the entities that can be obstructions.
	 */
	private List<Component> findPotentialObstructions() {

		// Calculate search box
		double elevation = parkingSpace.getElevation();
		double height = Math.max(parkingSpace.getHeight(), MIN_HEIGHT_TOLERANCE) - ELEVATION_TOLERANCE * 2;

		MAABB3d aabb = MAABB3d.create(parkingSpace.getAABB());
		aabb.resize(SIDE_OBSTRUCTION_DISTANCE);

		MVector3d min = MVector3d.create(aabb.getLowerBound());
		min.setZ(elevation + ELEVATION_TOLERANCE);

		MVector3d max = MVector3d.create(aabb.getUpperBound());
		max.setZ(min.getZ() + height);

		// Find obstructions within tolerance
		Collection<Component> obstructionEntities = SMC.getModel()
			.getComponents(AABBIntersectionFilter.of(MAABB3d.create(min, max)));
		obstructionEntities.removeIf(component -> !parkingObstructionsFilter.accept(component));

		return obstructionEntities.stream()
			.filter(entity -> !entity.equals(parkingSpace.getEntity()))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
