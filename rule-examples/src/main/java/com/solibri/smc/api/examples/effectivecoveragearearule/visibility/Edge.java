package com.solibri.smc.api.examples.effectivecoveragearearule.visibility;

import java.util.Objects;

class Edge<T> {
	final Vertex<T> source;
	final Vertex<T> destination;
	final double weight;

	Edge(Vertex<T> source, Vertex<T> destination) {
		this(source, destination, 0);
	}

	Edge(Vertex<T> source, Vertex<T> destination, double weight) {
		this.source = source;
		this.destination = destination;
		this.weight = weight;
	}

	T getSource() {
		return source.getPayload();
	}

	T getDestination() {
		return destination.getPayload();
	}

	/*
	 * Retrieves the other one of the vertices. If given source, it will return
	 * destination and if given destination, it will return source.
	 */
	T getOther(T v) {
		if (source.getPayload().equals(v)) {
			return destination.getPayload();
		} else if (destination.getPayload().equals(v)) {
			return source.getPayload();
		}

		throw new IllegalArgumentException(v + " is not the value at either vertex connected to this edge");
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, destination);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Edge)) {
			return false;
		}

		Edge<?> otherEdge = (Edge<?>) other;
		return source.equals(otherEdge.source) && destination.equals(otherEdge.destination);
	}

	@Override
	public String toString() {
		return "Edge: " + source + ", " + destination;
	}
}
