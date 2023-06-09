package com.solibri.smc.api.examples.effectivecoveragearearule.visibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.solibri.geometry.linearalgebra.MVector2d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.primitive2d.Area;
import com.solibri.geometry.primitive2d.MArea;
import com.solibri.geometry.primitive2d.MLine2d;
import com.solibri.geometry.primitive2d.MRay2d;
import com.solibri.geometry.primitive2d.Ray2d;
import com.solibri.geometry.primitive2d.Triangle2d;

/**
 * The VisibilityPolygonCalculator calculates the visibility polygons for
 * vertices of a visibility graph.
 */
public class VisibilityPolygonCalculator {

	final Optional<Area> effectSource;
	final Area originalArea;
	final EffectGeometryGraph geometryGraph;
	final VisibilityGraph visibilityGraph;
	final Map<Vector2d, Area> visibilityPolygonMap = new HashMap<>();

	public static VisibilityPolygonCalculator fromAreaAndSource(Area area, Area effectSource) {
		EffectGeometryGraph geometryGraph = EffectGeometryGraph.fromAreaAndSource(area, effectSource);
		VisibilityGraph visibilityGraph = VisibilityGraph.fromGeometry(geometryGraph);
		return new VisibilityPolygonCalculator(geometryGraph, visibilityGraph, area, effectSource);
	}

	static VisibilityPolygonCalculator fromAreaAndLocation(Area area, Vector2d source) {
		EffectGeometryGraph geometryGraph = EffectGeometryGraph.fromAreaAndLocation(area, source);
		VisibilityGraph visibilityGraph = VisibilityGraph.fromGeometry(geometryGraph);
		return new VisibilityPolygonCalculator(geometryGraph, visibilityGraph, area);
	}

	VisibilityPolygonCalculator(EffectGeometryGraph geometryGraph, VisibilityGraph visibilityGraph, Area area) {
		this.originalArea = area;
		this.geometryGraph = geometryGraph;
		this.visibilityGraph = visibilityGraph;
		this.effectSource = Optional.empty();
	}

	VisibilityPolygonCalculator(EffectGeometryGraph geometryGraph, VisibilityGraph visibilityGraph, Area area,
								Area effectSource) {
		this.originalArea = area;
		this.geometryGraph = geometryGraph;
		this.visibilityGraph = visibilityGraph;
		this.effectSource = Optional.of(MArea.create(effectSource));
	}

	/**
	 * Calculate the visibility polygon for a single vertex in the visibility
	 * graph.
	 *
	 * @param the vertex in the visibility graph for which we calculate the
	 * 	visibility polygon
	 *
	 * @return the visibility polygon
	 */
	public Area getVisibilityPolygonForSource(Vector2d source) {
		return visibilityPolygonMap.computeIfAbsent(source, this::calculateVisibilityPolygonForSource);
	}

	/**
	 * Calculate the visibility polygons for all vertices included in the
	 * geometry graph source vertices and combine the results.
	 *
	 * @return the combined visibility polygon for the source vertices in the
	 * 	geometry graph
	 */
	public Area getVisibilityPolygonForEffectSource() {
		visibilityGraph.calculate();
		List<MArea> areas = geometryGraph.getSourceVertices().stream()
			.map(this::getVisibilityPolygonForSource)
			.map(MArea::create)
			.collect(Collectors.toList());
		MArea finalArea = MArea.create(effectSource.orElseGet(MArea::create));
		for (MArea area : areas) {
			finalArea.add(area);
		}
		if (!this.originalArea.isEmpty()) {
			finalArea.intersect(this.originalArea);
		}
		return finalArea;
	}

	/**
	 * Calculate the visibility polygon for a single vertex in the visibility
	 * graph.
	 *
	 * The algorithm creates a ray to each vertex visible from the source and
	 * sorts them according to the clockwise angle related to positive x-axis.
	 * The rays are then iterated pairwise and a triangle is formed between each
	 * consecutive ray. The algorithm checks, how far it can extend the rays and
	 * creates a triangle that consists of the extended rays and the line
	 * connecting them. The triangles are merged in the end to form the
	 * visibility polygon.
	 *
	 * @param the vertex in the visibility graph for which we calculate the
	 * 	visibility polygon
	 *
	 * @return the visibility polygon
	 */
	Area calculateVisibilityPolygonForSource(Vector2d src) {
		/*
		 * Get visible vertices and assign a ray towards each of them. We sort
		 * the rays according to clockwise angle to x-axis. Add the first
		 * location as the last location for the while-loop to close.
		 */
		List<Vector2d> rayDestinations = visibilityGraph.getEdgesFromVertex(src).stream()
			.map(e -> e.getDestination())
			.sorted((dst1, dst2) -> {
				MVector2d dir1 = MVector2d.create(dst1);
				dir1.subtractInPlace(src);
				MVector2d dir2 = MVector2d.create(dst2);
				dir2.subtractInPlace(src);
				return Double.compare(dir1.getClockwiseAngle(), dir2.getClockwiseAngle());
			})
			.collect(Collectors.toList());
		if (!rayDestinations.isEmpty()) {
			rayDestinations.add(rayDestinations.get(0));
		}

		/*
		 * Iterate over the ray destinations
		 */
		List<Triangle2d> visibilityTriangles = new ArrayList<>();
		Iterator<Vector2d> iterator = rayDestinations.iterator();
		Vector2d curr = null;
		Vector2d prev = null;
		while (iterator.hasNext()) {
			curr = iterator.next();
			/*
			 * Check, if there is an edge from the source to both the current
			 * and previous vector in the geometry graph. If there exists such
			 * edges, this particular triangle is inside a hole or outside the
			 * polygon and as such needs to be discarded.
			 */
			if (prev != null && !geometryGraph.hasEdge(prev, src) && !geometryGraph.hasEdge(src, curr)) {
				/*
				 * Check if the previous and current vector have a common edge
				 * in the geometry graph. Since the geometry graph is directed,
				 * we need to check both directions. If there exists an edge,
				 * use that edge as part of the visibility triangle. Otherwise,
				 * create a triangle with sides that extend up to an
				 * intersection of some edge originating from either the current
				 * or previous location. If no such edge is found, extend the
				 * triangle very far.
				 */
				if (geometryGraph.hasEdge(curr, prev) || geometryGraph.hasEdge(prev, curr)) {
					visibilityTriangles.add(createTriangle(
						src,
						prev,
						curr));
				} else {
					visibilityTriangles.add(findExtendedTriangle(
						src,
						prev,
						curr));
				}
			}
			prev = curr;
		}

		/* Collect all the visibility triangles to one single Area */
		return visibilityTriangles.stream()
			.map(Triangle2d::toArea)
			.reduce(MArea.create(), (MArea accumulatedArea, MArea visibilityTriangle) -> {
				MArea sumOfAreas = MArea.create(accumulatedArea);
				sumOfAreas.add(visibilityTriangle);
				return sumOfAreas;
			});
	}

	/**
	 * Creates a triangle, as if base would be on the bottom in the middle, left
	 * in the top-left corner and right in the top-right corner. Edges run
	 * clockwise.
	 */
	Triangle2d createTriangle(Vector2d base, Vector2d left, Vector2d right) {
		return Triangle2d.create(
			left,
			right,
			base);
	}

	/**
	 * Creates an extended triangle, as if base would be on the bottom in the
	 * middle, left in the top-left corner and right in the top-right corner.
	 * Edges run clockwise.
	 *
	 * The triangle sides are extended up until they have an intersection with
	 * an edge originating from the left or right location. If no edge is
	 * obstructing the visibility, the triangle is extended very far.
	 */
	Triangle2d findExtendedTriangle(Vector2d base, Vector2d left, Vector2d right) {
		Optional<MVector2d> leftIntersection = findLeftEdgeIntersection(base, left, right);
		Optional<MVector2d> rightIntersection = findRightEdgeIntersection(base, left, right);

		if (leftIntersection.isPresent()) {
			return Triangle2d.create(
				leftIntersection.get(),
				right,
				base);
		} else if (rightIntersection.isPresent()) {
			return Triangle2d.create(
				left,
				rightIntersection.get(),
				base);
		} else {
			return createExtendedTriangle(base, left, right);
		}
	}

	Optional<MVector2d> findLeftEdgeIntersection(Vector2d base, Vector2d left, Vector2d right) {
		MLine2d rightLine = MLine2d.create(base, right);
		return findEdgeIntersection(
			base,
			left,
			right,
			e -> rightLine.isPointOnLeftSide(e.getOther(right)));
	}

	Optional<MVector2d> findRightEdgeIntersection(Vector2d base, Vector2d left, Vector2d right) {
		MLine2d leftLine = MLine2d.create(base, left);
		return findEdgeIntersection(
			base,
			right,
			left,
			e -> leftLine.isPointOnRightSide(e.getOther(left)));
	}

	/**
	 * Find the intersection between a ray along the edges of the triangle and
	 * some possible edge obstructing the ray.
	 *
	 * The ray for which we try to find the intersection originates from the
	 * source and targets towards the edgeDestination. The edges that can
	 * obstruct the ray have to originate from the other vertex in the triangle,
	 * the searchLocation.
	 *
	 * We filter the edges that might obstruct the ray and try to find an
	 * intersection between the edges and the ray. The intersection point
	 * closest to the source of the ray is picked.
	 *
	 * @param source the ray origin
	 * @param edgeDestination the destination towards which we aim the ray
	 * @param searchLocation the vertex in the geometry graph that might have
	 * 	obstructing edges connected to it
	 * @param edgeFilter the filter to find the possible obstructing edges
	 *
	 * @return possible intersection between the ray and an edge obstructing it
	 */
	Optional<MVector2d> findEdgeIntersection(Vector2d source, Vector2d edgeDestination, Vector2d searchLocation,
											 Predicate<Edge<Vector2d>> edgeFilter) {
		MVector2d edgeDir = MVector2d.create(edgeDestination);
		edgeDir.subtractInPlace(source);
		Ray2d edgeRay = MRay2d.create(source, edgeDir);
		return Stream.concat(
			geometryGraph.getEdgesFromVertex(searchLocation).stream(),
			geometryGraph.getEdgesToVertex(searchLocation).stream())
			.filter(edgeFilter)
			.map(edge -> {
				MVector2d obstructionDir = MVector2d.create(edge.getDestination());
				obstructionDir.subtractInPlace(edge.getSource());
				Ray2d obstructionRay = MRay2d.create(edge.getSource(), obstructionDir);
				Optional<MVector2d> intersectionPoint = obstructionRay.intersect(edgeRay);
				if (intersectionPoint.isPresent()) {
					return intersectionPoint.get();
				} else {
					// ray and obstruction are parallel, return a point far away
					MVector2d edgeDirCopy = MVector2d.create(edgeRay.getDirection());
					edgeDirCopy.scaleInPlace(10.0);
					edgeDirCopy.addInPlace(edgeRay.getOrigin());
					return edgeDirCopy;
				}
			})
			.sorted((v1, v2) -> Double
				.compare(v1.distanceSquared(edgeRay.getOrigin()), v2.distanceSquared(edgeRay.getOrigin())))
			.findFirst();
	}

	/**
	 * Creates a triangle, as if base would be on the bottom in the middle, left
	 * in the top-left corner and right in the top-right corner. Edges run
	 * clockwise.
	 *
	 * The triangle sides are extended far.
	 */
	Triangle2d createExtendedTriangle(Vector2d base, Vector2d left, Vector2d right) {
		MVector2d leftDir = MVector2d.create(left);
		leftDir.subtractInPlace(base);
		MVector2d rightDir = MVector2d.create(right);
		rightDir.subtractInPlace(base);
		final double coefficient = 10.0;
		leftDir.scaleInPlace(coefficient);
		rightDir.scaleInPlace(coefficient);
		leftDir.addInPlace(base);
		rightDir.addInPlace(base);
		return Triangle2d.create(
			base,
			leftDir,
			rightDir);
	}
}
