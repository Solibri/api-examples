package com.solibri.smc.api.examples.beginner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.solibri.geometry.linearalgebra.Vector3d;
import com.solibri.geometry.mesh.TriangleMesh;
import com.solibri.geometry.primitive3d.Segment3d;
import com.solibri.geometry.primitive3d.Triangle3d;
import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.visualization.VisualizationItem;

/**
* An example check that visualizes all the max distances of all component pairs in a
* component set; each max distance comes from a pair of components in the component set.
*
* A max distance is calculated using the maximum distance between all the points
* in a component pair.
 */
public final class DistanceVisualizationRule extends OneByOneRule {

	/**
	 * Retrieve the parameter creation handler, used to define parameters for
	 * this rule.
	 */
	private final RuleParameters params = RuleParameters.of(this);

	/**
	 * Retrieve the default filter.
	 * Every component that passes the filter is then forwarded to
	 * the {@link OneByOneRule#check(Component, ResultFactory)} method.
	 */
	final FilterParameter rpComponentFilter = this.getDefaultFilterParameter();

	final FilterParameter rpComponentFilter2 = params.createFilter("rpComponentFilter2");

	final DoubleParameter maximumDistance = params.createDouble("rpMaximumDistance", PropertyType.LENGTH);

	/**
	 * Add the UI definition from class DistanceVisualizationRuleUIDefinition.
	 */
	private final DistanceVisualizationRuleUIDefinition uiDefinition = new DistanceVisualizationRuleUIDefinition(this);

	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {

		// Search only for components that are in range of the maximumDistance parameter.
		double searchDistance = maximumDistance.getValue();
		ComponentFilter filter2 = rpComponentFilter2.getValue();
		ComponentFilter withinDistanceFilter =
			AABBIntersectionFilter.ofComponentBounds(component, searchDistance, searchDistance).and(filter2);
		Collection<Component> targets = SMC.getModel().getComponents(withinDistanceFilter);

		List<Result> results = new ArrayList<>();

		// Check each component separately.
		for (Component target : targets) {
			results.add(distanceCheck(component, target, resultFactory));
		}

		return results;
	}

	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}

	/**
	 * Returns the longest segment between two components. The length of the longest segment is the maximum distance
	 * between the components.
	 *
	 * @param component1 the first component
	 * @param component2 the second component
	 * @return the longest segment between any points of two components
	 */
	private Segment3d getLongestSegment(Component component1, Component component2) {
		/*
		 * The triangles in component's geometry can be looped with the TriangleMesh used as an Iterable of triangles.
		 */
		TriangleMesh sourceMesh = component1.getTriangleMesh();
		TriangleMesh targetMesh = component2.getTriangleMesh();

		Segment3d longestSegment = null;
		for (Triangle3d sourceTriangle : sourceMesh) {
			for (Triangle3d targetTriangle : targetMesh) {
				Segment3d potentialLongestSegment = getLongestSegment(sourceTriangle, targetTriangle);
				if (longestSegment == null || potentialLongestSegment.getLength() > longestSegment.getLength()) {
					longestSegment = potentialLongestSegment;
				}
			}
		}
		return longestSegment;
	}

	/**
	 * Returns the longest segment between two triangles.
	 *
	 * @param triangle1 the first triangle
	 * @param triangle2 the second triangle
	 * @return the longest segment between any points of two triangles
	 */
	private Segment3d getLongestSegment(Triangle3d triangle1, Triangle3d triangle2) {
		Segment3d longestSegment = null;
		for (Vector3d firstPoint : triangle1) {
			for (Vector3d secondPoint : triangle2) {
				double potentialDistance = firstPoint.distance(secondPoint);
				if (longestSegment == null || potentialDistance > longestSegment.getLength()) {
					longestSegment = Segment3d.create(firstPoint, secondPoint);
				}
			}
		}
		return longestSegment;
	}

	private Result distanceCheck(Component source, Component target, ResultFactory results) {
		Segment3d longestSegment = getLongestSegment(source, target);

		String nameSource = source.getName();
		String nameTarget = target.getName();
		String distanceText = PropertyType.LENGTH.getFormat().format(longestSegment.getLength());

		// Create the result.
		return results
			.create(
				nameSource + " " + nameTarget + " " + distanceText,
				"Distance between " + nameSource + " " + nameTarget + " is " + distanceText)
			.withInvolvedComponent(target)
			.withVisualization(visualization -> {
				// Create visualization using provided factory that can create dimensional lines.
				List<VisualizationItem> dimensionLine = VisualizationItem
					.createDimension(longestSegment.getStartPoint(), longestSegment.getEndPoint());
				visualization.addVisualizationItems(dimensionLine);
				visualization.addComponent(source, 0.5);
				visualization.addComponent(target, 0.5);
			});
	}
}
