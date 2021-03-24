package com.solibri.smc.api.examples.effectivecoveragearearule.visibility;

import java.util.Collection;
import java.util.Optional;

import com.solibri.geometry.linearalgebra.MVector2d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.primitive2d.MSegment2d;
import com.solibri.geometry.primitive2d.Segment2d;

/**
 * The visibility graph takes a geometry graph as an argument. It calculates for
 * each vertex of the geometry graph, what other vertices have a direct line of
 * sight to that vertex. For each such vertex, it assigns an edge between the
 * two vertices.
 */
class VisibilityGraph extends DirectedGraph<Vector2d> {

	final EffectGeometryGraph geometryGraph;

	static VisibilityGraph fromGeometry(EffectGeometryGraph geometryGraph) {
		return new VisibilityGraph(geometryGraph);
	}

	VisibilityGraph(EffectGeometryGraph geometryGraph) {
		this.geometryGraph = geometryGraph;
	}

	public void calculate() {
		Collection<Vector2d> verts = geometryGraph.getVertices();

		// Add vertices to the visibility graph
		verts.forEach(this::addVertex);

		/*
		 * Brute force implementation, replace with for example Lee's algorithm,
		 * if necessary
		 */
		for (Vector2d src : verts) {
			for (Vector2d dst : verts) {
				if (!src.equals(dst) &&
					isValidDirectionFromVertex(src, dst) &&
					isVisibleFromVertex(src, dst)) {
					addEdge(src, dst, src.distance(dst));
				}
			}
		}
	}

	/**
	 * Not all directions are proper visible directions from the source vertex.
	 * For example, vertices cannot be seen through holes or outside the polygon
	 * geometry.
	 *
	 * To find, if a direction a ray is cast from the source is valid, we can
	 * utilize the information that polygon edges run clockwise and hole edges
	 * counterclockwise. We also know that a vertex can be the source or the
	 * destination in only one edge in the geometry graph.
	 *
	 * The angle of the direction should thus be between the source edge vector
	 * and the opposite of the destination edge vector clockwise. This way we
	 * can take into account both the polygon and hole vertices.
	 */
	boolean isValidDirectionFromVertex(Vector2d src, Vector2d dst) {

		Optional<Edge<Vector2d>> srcAsSrc = geometryGraph.getEdgesFromVertex(src).stream().findAny();
		Optional<Edge<Vector2d>> srcAsDst = geometryGraph.getEdgesToVertex(src).stream().findAny();
		if (srcAsSrc.isPresent() && srcAsDst.isPresent()) {
			/*
			 * Calculate the clockwise angle from x-axis for the source edge
			 * vector and destination edge vector and rotate the angles so that
			 * srcVec is aligned with x-axis
			 */
			MVector2d srcVec = srcAsSrc.get().getDestination().subtract(srcAsSrc.get().getSource());
			MVector2d dstVec = srcAsDst.get().getSource().subtract(srcAsDst.get().getDestination());
			MVector2d dir = dst.subtract(src);

			double srcAngle = srcVec.getClockwiseAngle();
			double dstAngle = dstVec.getClockwiseAngle() - srcAngle;
			double dirAngle = dir.getClockwiseAngle() - srcAngle;

			if (dstAngle < 0.0) {
				dstAngle += 2 * Math.PI;
			}
			if (dirAngle < 0.0) {
				dirAngle += 2 * Math.PI;
			}

			return dirAngle <= dstAngle;
		}
		/*
		 * The vertex has only one edge or no edges connected to it in the
		 * geometry graph, so all directions are valid.
		 */
		return true;
	}

	/**
	 * Loop over the edges in the geometry graph and check, if the line between
	 * source and destination intersects any of them.
	 */
	boolean isVisibleFromVertex(Vector2d src, Vector2d dst) {
		Segment2d srcToDestSegment = MSegment2d.create(src, dst);
		return !geometryGraph.getEdges().stream()
			.filter(e -> {
				/*
				 * If the ray goes through the source or destination, it doesn't
				 * cause an intersection
				 */
				if (e.getSource().equals(src) || e.getDestination().equals(src) || e.getSource().equals(dst) || e
					.getDestination().equals(dst)) {
					return false;
				}
				Segment2d edgeSegment = MSegment2d.create(e.getSource(), e.getDestination());
				return srcToDestSegment.intersects(edgeSegment);
			})
			.findAny()
			.isPresent();
	}
}
