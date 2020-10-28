package com.solibri.smc.api.examples.parkingrule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.solibri.geometry.algorithms.ConvexHull;
import com.solibri.geometry.linearalgebra.MVector2d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.linearalgebra.Vector3d;
import com.solibri.geometry.primitive2d.Line2d;
import com.solibri.geometry.primitive2d.MLine2d;
import com.solibri.geometry.primitive2d.MPolygon2d;
import com.solibri.geometry.primitive2d.MSegment2d;
import com.solibri.geometry.primitive2d.Rectangle2d;

/**
 * Class that provides a method for finding the approximately maximum area
 * rectangle that can fit inside a given polygon.
 */
final class MaximumRectangle {

	private static final double LENGHT_TOLERANCE = 1e-3;
	private static final int MAX_ITERATIONS = 100;

	private MaximumRectangle() {

	}

	private static final class RectangleSegment {

		private static final double MOVE_DISTANCE_TOLERANCE = 0.95; // 95%

		private static final double MIN_ACCEPTED_DISTANCE = 1e-6;

		private final MVector2d point1;

		private final MVector2d point2;

		private MVector2d movePoint = MVector2d.create();

		private double moveDistance = Double.MAX_VALUE;

		private double maximumMoveDistance = Double.MAX_VALUE;

		private RectangleSegment(MVector2d point1, MVector2d point2) {
			this.point1 = point1;
			this.point2 = point2;
		}

		private void setDistanceToTheClosestPoint(Vector2d[] points) {
			moveDistance = Double.MAX_VALUE;
			for (Vector2d p : points) {
				Line2d line = MLine2d.create(point1, point2);
				if (line.isPointOnLeftSide(p)) {
					double dist = line.distance(p);
					if (dist > MIN_ACCEPTED_DISTANCE && dist < moveDistance) {
						movePoint.set(p);
						moveDistance = dist;
					}
				}
			}
		}

		private boolean isMovable() {
			return moveDistance < MOVE_DISTANCE_TOLERANCE * maximumMoveDistance;
		}

		private double getLength() {
			return point1.distance(point2);
		}

		private void move() {
			maximumMoveDistance -= moveDistance;
			Vector2d projectedPoint = MLine2d.create(point1, point2).project(movePoint);
			Vector2d movement = movePoint.subtract(projectedPoint);
			point1.addInPlace(movement);
			point2.addInPlace(movement);
		}
	}

	/**
	 * Returns an approximately maximum area rectangle that can fit inside the
	 * given polygon. There is no guarantee that the rectangle returned by this
	 * method is optimal or even close to optimal, although for most
	 * quadrilateral polygons the results are adequate.
	 * <p>
	 * The algorithms works in the following way:
	 * <ol>
	 * <li>Compute the minimum area bounding rectangle for the polygon</li>
	 * <li>Find the segment of the rectangle that is closest to a polygon vertex
	 * that is not already on one of the segments of the rectangle</li>
	 * <li>Move the segment onto the vertex of the polygon found in the above
	 * step</li>
	 * <li>Repeat the two above steps until all vertices of the rectangle are
	 * inside the polygon</li>
	 * </ol>
	 * </p>
	 *
	 * @param polygon The polygon inside which the rectangle is fitted
	 *
	 * @return a rectangle of approximately maximum area such that all of its
	 * 	vertices are inside the polygon
	 */
	static Vector3d[] findMaximumRectangle(Vector2d[] polygon) {

		MVector2d[] polygonCopy = Arrays.stream(polygon).map(vertex -> MVector2d.create(vertex))
			.toArray(MVector2d[]::new);

		MPolygon2d convexHull = ConvexHull.of(Arrays.asList(polygonCopy));
		Rectangle2d rectangleObject = convexHull.getOrientedBoundingRectangle();
		MVector2d[] rectangleVertices = rectangleObject.getVertices()
			.toArray(new MVector2d[rectangleObject.getVertexCount()]);

		/*
		 * Make sure the rectangle is counter clock wise in order to check if a
		 * point is on the left side of the line segment.
		 */
		List<MVector2d> rectangleVerticesList = Arrays.asList(rectangleVertices);
		if (!MPolygon2d.create(rectangleVerticesList).isCounterClockwise()) {
			Collections.reverse(rectangleVerticesList);
		}

		RectangleSegment[] segments = createSegments(rectangleVertices);
		RectangleSegment segmentToMove = findSegmentToMove(segments, polygon);

		for (int iterationCount = 0; iterationCount < MAX_ITERATIONS && segmentToMove != null; iterationCount++) {
			segmentToMove.move();

			if (areAllPointsInside(rectangleVertices, polygon)) {
				segmentToMove = null;
				break;
			}

			segmentToMove = findSegmentToMove(segments, polygon);
		}

		boolean found = segmentToMove == null;

		if (found) {
			return new Vector3d[] {
				rectangleVertices[0].to3dVector(), rectangleVertices[1].to3dVector(),
				rectangleVertices[2].to3dVector(), rectangleVertices[3].to3dVector(),};
		}
		return null;
	}

	private static boolean areAllPointsInside(Vector2d[] points, Vector2d[] polygonPoints) {
		MPolygon2d polygon = MPolygon2d.create(Arrays.asList(polygonPoints));

		for (Vector2d point : points) {
			if (!polygon.contains(point)) {
				return false;
			}
		}

		return true;
	}

	private static RectangleSegment[] createSegments(MVector2d[] rectangle) {
		RectangleSegment[] segments = new RectangleSegment[4];
		for (int i = 0; i < 4; i++) {
			int j = (i + 1) % 4;
			segments[i] = new RectangleSegment(rectangle[i], rectangle[j]);
		}

		return segments;
	}

	private static void updateMaximumDistances(RectangleSegment[] segments) {
		for (int i = 0; i < segments.length; i++) {
			int j = (i + 1) % segments.length;
			double length = segments[j].getLength();
			segments[i].maximumMoveDistance = length;
		}
	}

	private static RectangleSegment findSegmentToMove(RectangleSegment[] segments, Vector2d[] polygon) {
		RectangleSegment toMove = null;
		updateMaximumDistances(segments);
		for (RectangleSegment segment : segments) {

			if (!isPointInPolygon(segment.point1, polygon, LENGHT_TOLERANCE) || !isPointInPolygon(segment.point2,
				polygon, LENGHT_TOLERANCE)) {
				segment.setDistanceToTheClosestPoint(polygon);

				if (segment.isMovable()) {
					if (toMove == null || segment.moveDistance < toMove.moveDistance) {
						toMove = segment;
					}
				}
			}
		}

		return toMove;
	}

	private static boolean isPointInPolygon(final Vector2d point, final Vector2d[] polygon, double tolerance) {
		if (MPolygon2d.create(Arrays.asList(polygon)).contains(point)) {
			return true;
		}

		double min = Double.MAX_VALUE;
		MSegment2d segment = MSegment2d.create(MVector2d.create(), MVector2d.create());

		for (int i = 0; i < polygon.length; i++) {
			int j = (i + 1) % polygon.length;
			segment.set(polygon[i], polygon[j]);
			double newMin = segment.distance(point);
			min = Math.min(newMin, min);
		}

		return min < tolerance;
	}
}
