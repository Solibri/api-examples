package com.solibri.smc.api.examples.effectivecoveragearearule.visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.solibri.geometry.linearalgebra.MVector2d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.primitive2d.Area;
import com.solibri.geometry.primitive2d.MArea;
import com.solibri.geometry.primitive2d.MPolygon2d;
import com.solibri.geometry.primitive2d.MSegment2d;
import com.solibri.geometry.primitive2d.Polygon2d;
import com.solibri.geometry.primitive2d.Segment2d;

/**
 * The effect geometry graph is a graph representation of a space footprint
 * polygon with additional information about what vertices belong to the effect
 * source.
 */
public class EffectGeometryGraph extends DirectedGraph<Vector2d> {

	private final Set<Vector2d> sourceVertices = new HashSet<>();

	private Area possiblyResizedSource;

	private static final int SOURCE_SAMPLING_ACCURACY = 9;

	static EffectGeometryGraph fromAreaAndSource(Area area, Area source) {
		EffectGeometryGraph graph = new EffectGeometryGraph();
		MArea areaCopyWithSource = MArea.create(area);
		MArea areaCopyWithoutSource = MArea.create(area);
		MArea sourceCopy = MArea.create(source);

		/*
		 * Set source as a hole in the geometry and use only the parts that
		 * intersect the original area
		 */
		areaCopyWithoutSource.subtract(source);
		sourceCopy.intersect(area);

		// If the effect source is modelled right next to the space, we need to
		// scale it up a bit, so it intersects with the space.
		if (sourceCopy.isEmpty()) {
			areaCopyWithoutSource = MArea.create(area);
			sourceCopy = MArea.create(source);
			sourceCopy.resize(Math.sqrt(sourceCopy.getSize()) / 10);

			/*
			 * Set source as a hole in the geometry and use only the parts that
			 * intersect the original area
			 */
			areaCopyWithoutSource.subtract(source);
			sourceCopy.intersect(area);
		}

		graph.possiblyResizedSource = sourceCopy;

		// add source vertices
		List<Vector2d> sourceVertices = new ArrayList<>();
		sourceCopy.getPolygons().stream()
			.flatMap(polygon -> polygon.getVertices().stream())
			.forEach(vertex -> {
				sourceVertices.add(vertex);
			});

		List<Vector2d> allVectors = getBordersWithSampling(sourceVertices, SOURCE_SAMPLING_ACCURACY);
		Collections.reverse(allVectors);
		MPolygon2d sourceHole = MPolygon2d.create(allVectors);

		// add polygons
		Stream.concat(areaCopyWithSource.getPolygons().stream(),
			areaCopyWithSource.getHoles().stream()).forEach(graph::addPolygon);
		graph.addPolygon(sourceHole);

		// Create edges.
		for (Vector2d vector : allVectors) {
			graph.addVertex(vector);
			graph.sourceVertices.add(vector);
		}

		return graph;
	}

	/**
	 * Takes source vertices and samples the border they make up with given
	 * accuracy.
	 *
	 * @param sourceVertices the original source vertices
	 * @param amountOfSamples the amount of new samples
	 *
	 * @return the new list of source vertices
	 */
	private static List<Vector2d> getBordersWithSampling(List<Vector2d> sourceVertices, int amountOfSamples) {
		if (sourceVertices.isEmpty()) {
			return new ArrayList<>();
		}
		// Sample the border.
		Vector2d previousVector = null;
		List<Vector2d> allVectors = new ArrayList<>();
		allVectors.add(sourceVertices.get(0));
		for (Vector2d vector : sourceVertices) {
			if (previousVector != null) {
				Segment2d segment = MSegment2d.create(previousVector, vector);
				List<MVector2d> sampledPoints = segment.sample(amountOfSamples);

				allVectors.addAll(sampledPoints);
				allVectors.add(vector);
			}

			previousVector = vector;
		}

		// Add the last border line, its sampled points and the first corner.
		Segment2d segment = MSegment2d.create(sourceVertices.get(sourceVertices.size() - 1), sourceVertices.get(0));
		List<MVector2d> sampledPoints = segment.sample(amountOfSamples);
		// allVectors.add(sourceVertices.get(sourceVertices.size() - 1));
		allVectors.addAll(sampledPoints);
		return allVectors;
	}

	static EffectGeometryGraph fromAreaAndLocation(Area area, Vector2d source) {
		EffectGeometryGraph graph = new EffectGeometryGraph();

		// add polygons
		Stream.concat(area.getPolygons().stream(), area.getHoles().stream()).forEach(graph::addPolygon);

		// add source vertex
		graph.addVertex(source);
		graph.sourceVertices.add(source);
		return graph;
	}

	void addPolygon(Polygon2d polygon) {
		Iterator<Segment2d> iterator = polygon.getEdgeIterator();

		// Loop over the edges and add them to the graph
		while (iterator.hasNext()) {
			Segment2d edge = iterator.next();
			addEdge(edge.getStartPoint(), edge.getEndPoint());
		}
	}

	/**
	 * Return the source vertices in an unmodifiable collection.
	 *
	 * @return the unmodifiable set of the source vertices
	 */
	public Set<Vector2d> getSourceVertices() {
		return Collections.unmodifiableSet(sourceVertices);
	}

	public Area getPossiblyResizedSource() {
		return possiblyResizedSource;
	}

	public void setPossiblyResizedSource(Area possiblyResizedSource) {
		this.possiblyResizedSource = possiblyResizedSource;
	}
}
