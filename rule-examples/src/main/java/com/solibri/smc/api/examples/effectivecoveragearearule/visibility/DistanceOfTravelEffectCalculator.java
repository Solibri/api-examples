package com.solibri.smc.api.examples.effectivecoveragearearule.visibility;

import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.primitive2d.Area;
import com.solibri.geometry.primitive2d.MArea;

/**
 * The DistanceOfTravelEffectCalculator calculates the coverage area, when the
 * effect can "move" around corners. When the effect moves around a corner, the
 * remaining effect range is defined as the shortest distance to the effect
 * source substracted from the original effect range.
 */
public class DistanceOfTravelEffectCalculator {

	final EffectGeometryGraph geometryGraph;

	public EffectGeometryGraph getGeometryGraph() {
		return geometryGraph;
	}

	final VisibilityGraph visibilityGraph;
	final VisibilityPolygonCalculator calculator;
	final Area effectSource;
	final double effectRange;
	final Map<Vector2d, Double> distanceToSourceMap = new HashMap<>();

	public static DistanceOfTravelEffectCalculator fromAreaAndSourceAndEffectRange(Area area, Area source,
																				   double effectRange) {
		EffectGeometryGraph geometryGraph = EffectGeometryGraph.fromAreaAndSource(area, source);
		VisibilityGraph visibilityGraph = VisibilityGraph.fromGeometry(geometryGraph);
		DistanceOfTravelEffectCalculator calculator = new DistanceOfTravelEffectCalculator(geometryGraph,
			visibilityGraph, source, effectRange);
		calculator.initialize();
		return calculator;
	}

	DistanceOfTravelEffectCalculator(EffectGeometryGraph geometryGraph, VisibilityGraph visibilityGraph,
									 Area effectSource, double effectRange) {
		this.geometryGraph = geometryGraph;
		this.visibilityGraph = visibilityGraph;
		this.effectSource = effectSource;
		this.effectRange = effectRange;
		this.calculator = new VisibilityPolygonCalculator(geometryGraph, visibilityGraph, effectSource);
	}

	/**
	 * To initialize the calculator, first the VisibilityGraph is calculated.
	 * Then the vertices that are within the effect range from the effect source
	 * are inserted to the distanceToSourceMap.
	 */
	void initialize() {
		// calculate the visibility graph
		visibilityGraph.calculate();

		/*
		 * Initialize map with all vertices in visibility graph. Set distance
		 * for source vertices to 0.0 and add them to the queue.
		 */
		Queue<Vector2d> queue = new LinkedList<>();
		visibilityGraph.getVertices().forEach(vertex -> {
			if (geometryGraph.getSourceVertices().contains(vertex)) {
				queue.add(vertex);
				distanceToSourceMap.put(vertex, 0.0);
			} else {
				distanceToSourceMap.put(vertex, Double.MAX_VALUE);
			}
		});

		/*
		 * Breadth-first search to find all vertices that are within effect
		 * range of source in the visibility graph
		 */
		while (!queue.isEmpty()) {
			Vector2d source = queue.remove();
			double currentRange = distanceToSourceMap.get(source);
			visibilityGraph.getEdgesFromVertex(source)
				.forEach(edge -> {
					double range = currentRange + edge.weight;
					Vector2d destination = edge.getDestination();
					if (range <= effectRange && range < distanceToSourceMap.get(destination)) {
						distanceToSourceMap.put(destination, range);
						queue.add(destination);
					}
				});
		}
	}

	/**
	 * Calculate the effect areas for all the vertices that are within the
	 * effect range from the effect source and add them together to form the
	 * effect area.
	 *
	 * @return the combined effect areas for each vertex within effect range
	 * 	from effect source
	 */
	public MArea getEffectArea() {
		return distanceToSourceMap.entrySet().stream()
			.filter(entry -> entry.getValue() <= effectRange)
			.map(entry -> calculateEffectAreaForSource(entry.getKey(), entry.getValue()))
			.reduce(MArea.create(effectSource),
				(accumulatedArea, visibilityPolygon) -> {
					MArea sumOfAreas = MArea.create(accumulatedArea);
					sumOfAreas.add(visibilityPolygon);
					return sumOfAreas;
				});
	}

	/**
	 * Get the effect area for a single source vertex in the visibility graph.
	 *
	 * @param source the vertex in the visibility graph for which we calculate
	 * 	the effect area
	 * @param distanceToSource the distance to the effect source in the
	 * 	visibility graph
	 *
	 * @return the effect area
	 */
	MArea calculateEffectAreaForSource(Vector2d source, double distanceToSource) {
		double rangeFromVertex = effectRange - distanceToSource;
		MArea visibilityPolygon = MArea.create(calculator.getVisibilityPolygonForSource(source));
		MArea circle = getPolygonCircle(source, rangeFromVertex);
		visibilityPolygon.intersect(circle);
		return visibilityPolygon;
	}

	/**
	 * AWT doesn't handle well intersections for paths with second- or
	 * third-order curves. For this reason, it's better to use a polygon
	 * approximation of a circle instead of Ellipse2D.Double.
	 *
	 * This method approximates a circle with the given centerpoint and radius
	 * with a regular 36-sided inscribed polygon.
	 *
	 * @param center the centerpoint of the circle
	 * @param radius the radius of the circle
	 *
	 * @return a mutable area that approximates the circle with a 36-sided
	 * 	inscribed regular polygon
	 */
	MArea getPolygonCircle(Vector2d center, double radius) {
		Path2D.Double path = new Path2D.Double();
		path.moveTo(center.getX() + radius, center.getY());
		int nGon = 36;
		for (int i = 1; i < nGon; i++) {
			double angle = 2 * i * Math.PI / nGon;
			path.lineTo(center.getX() + radius * Math.cos(angle), center.getY() + radius * Math.sin(angle));
		}
		path.closePath();
		return MArea.create(path);
	}
}
