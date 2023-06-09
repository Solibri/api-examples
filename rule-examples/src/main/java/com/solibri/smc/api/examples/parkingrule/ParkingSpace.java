package com.solibri.smc.api.examples.parkingrule;

import java.util.Arrays;

import com.solibri.geometry.linearalgebra.MVector3d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.linearalgebra.Vector3d;
import com.solibri.geometry.primitive2d.Area;
import com.solibri.geometry.primitive2d.MArea;
import com.solibri.geometry.primitive3d.AABB3d;
import com.solibri.geometry.primitive3d.MSegment3d;
import com.solibri.geometry.primitive3d.Rectangle3d;
import com.solibri.geometry.primitive3d.Segment3d;
import com.solibri.smc.api.model.Component;

final class ParkingSpace {

	private static final double MINIMUM_VALID_PARKING_AREA = 1.0;

	private final double width;

	private final double length;

	private final Segment3d longerSegment;

	private final Segment3d shorterSegment;

	private final Area rectangleArea;

	private final Rectangle3d rectangle;

	private final Vector2d[] footprint;

	private final Component entity;

	private final AABB3d aabb;

	ParkingSpace(Component uuid, final Vector2d[] footprint, final Vector3d[] rectangle, final AABB3d aabb) {
		this.entity = uuid;
		this.footprint = footprint;
		this.rectangle = Rectangle3d.create(Arrays.asList(rectangle));
		this.aabb = aabb;

		this.width = this.rectangle.getShorterRectangleSegmentLength();
		this.length = this.rectangle.getLongerRectangleSegmentLength();

		this.rectangleArea = MArea.create(Vector2d.to2dVectors(Arrays.asList(rectangle)));

		Segment3d longerSegment = this.rectangle.getLongerRectangleSegment();
		MVector3d longerSegment1 = MVector3d.create(longerSegment.getStartPoint());
		longerSegment1.setZ(getElevation());
		MVector3d longerSegment2 = MVector3d.create(longerSegment.getEndPoint());
		longerSegment2.setZ(getElevation());

		Segment3d shorterSegment = this.rectangle.getShorterRectangleSegment();
		MVector3d shorterSegment1 = MVector3d.create(shorterSegment.getStartPoint());
		shorterSegment1.setZ(getElevation());
		MVector3d shorterSegment2 = MVector3d.create(shorterSegment.getEndPoint());
		shorterSegment2.setZ(getElevation());

		this.longerSegment = MSegment3d.create(longerSegment1, longerSegment2);
		this.shorterSegment = MSegment3d.create(shorterSegment1, shorterSegment2);
	}

	public Component getEntity() {
		return entity;
	}

	double getWidth() {
		return width;
	}

	double getLength() {
		return length;
	}

	double getHeight() {
		return aabb.getSizeZ();
	}

	public double getElevation() {
		return aabb.getLowerBound().getZ();
	}

	public Segment3d getLongerSegment() {
		return longerSegment;
	}

	public Segment3d getShorterSegment() {
		return shorterSegment;
	}

	public Area getRectangleArea() {
		return rectangleArea;
	}

	public Rectangle3d getRectangle() {
		return rectangle;
	}

	public Vector2d[] getFootprint() {
		return footprint;
	}

	public AABB3d getAABB() {
		return aabb;
	}

	public boolean isValid() {
		// To be valid the parking area should be at least 1 squared meters
		return !rectangleArea.isEmpty() && rectangleArea.getSize() >= MINIMUM_VALID_PARKING_AREA;
	}
}