package com.solibri.smc.api.examples.effectivecoveragearearule.visibility;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class Vertex<T> {
	/*
	 * The payload should be immutable, since changing the data in the payload
	 * could affect the hash code and thus could lead to issues in retrieving
	 * the vertices from the DirectedGraph hash map.
	 */
	private final T payload;
	final Set<Edge<T>> sources = new HashSet<>();
	final Set<Edge<T>> destinations = new HashSet<>();

	Vertex(T payload) {
		this.payload = payload;
	}

	public T getPayload() {
		return payload;
	}

	public Set<Edge<T>> getSources() {
		return Collections.unmodifiableSet(sources);
	}

	public Set<Edge<T>> getDestinations() {
		return Collections.unmodifiableSet(destinations);
	}

	@Override
	public int hashCode() {
		return payload.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Vertex) {
			return payload.equals(((Vertex<?>) other).payload);
		}
		return false;
	}

	@Override
	public String toString() {
		return "Vertex: " + payload;
	}
}
