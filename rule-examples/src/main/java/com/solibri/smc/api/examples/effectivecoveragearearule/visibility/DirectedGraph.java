package com.solibri.smc.api.examples.effectivecoveragearearule.visibility;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * A generic implementation of a directed graph data structure.
 *
 * It is meant to be used with immutable data objects that
 * should have a proper implementation of equals() and hashCode().
 */
class DirectedGraph<T> {
	private final Set<Edge<T>> edges = new HashSet<>();
	private final Map<T, Vertex<T>> dataToVertex = new HashMap<>();

	DirectedGraph() {
	}

	void addGraph(DirectedGraph<T> other) {
		other.dataToVertex.keySet().forEach(this::addVertex);
		other.edges.forEach(edge -> addEdge(edge.getSource(), edge.getDestination()));
	}

	void addVertex(T payload) {
		if (!dataToVertex.containsKey(payload)) {
			Vertex<T> newVertex = new Vertex<>(payload);
			dataToVertex.put(payload, newVertex);
		}
	}

	void addEdge(T source, T destination, double weight) {
		addVertex(source);
		addVertex(destination);
		Edge<T> edge = new Edge<>(dataToVertex.get(source), dataToVertex.get(destination), weight);
		edge.source.destinations.add(edge);
		edge.destination.sources.add(edge);
		edges.add(edge);
	}

	void addEdge(T source, T destination) {
		addEdge(source, destination, 0.0);
	}

	Set<Edge<T>> getEdgesFromVertex(T payload) {
		if (!dataToVertex.containsKey(payload)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(dataToVertex.get(payload).destinations);
	}

	Set<Edge<T>> getEdgesToVertex(T payload) {
		if (!dataToVertex.containsKey(payload)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(dataToVertex.get(payload).sources);
	}

	boolean hasEdge(T source, T destination) {
		return edges.contains(new Edge<>(new Vertex<>(source), new Vertex<>(destination)));
	}

	Collection<T> getVertices() {
		return Collections.unmodifiableCollection(dataToVertex.keySet());
	}

	Set<Edge<T>> getEdges() {
		return Collections.unmodifiableSet(this.edges);
	}
}
