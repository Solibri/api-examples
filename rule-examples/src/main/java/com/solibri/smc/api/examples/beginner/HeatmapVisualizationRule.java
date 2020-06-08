package com.solibri.smc.api.examples.beginner;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.solibri.geometry.linearalgebra.MVector3d;
import com.solibri.geometry.linearalgebra.Vector2d;
import com.solibri.geometry.linearalgebra.Vector3d;
import com.solibri.geometry.primitive2d.AABB2d;
import com.solibri.geometry.primitive2d.Area;
import com.solibri.geometry.primitive2d.MAABB2d;
import com.solibri.geometry.primitive2d.Polygon2d;
import com.solibri.geometry.primitive3d.AABB3d;
import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.visualization.Bitmap;

/**
 * Example rule template that uses Bitmap visualization to visualize heatmap.
 */
public class HeatmapVisualizationRule extends OneByOneRule {

	/**
	 * Light blue color for heatmap visualization.
	 */
	private static final Color VISUALIZATION_COLOR = new Color(100, 100, 255);

	/**
	 * Resolution of the bitmap.
	 */
	private static final int BITMAP_RESOLUTION = 1600;

	/**
	 * Maximum number of the heatmap color steps used.
	 */
	private static final int STEP_LIMIT = 50;

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

	/**
	 * The second filter for the effect components.
	 */
	final FilterParameter rpEffectSourceFilter = params.createFilter("rpEffectSourceFilter");

	/**
	 * A DoubleParameter allows the user to input a double value that in this
	 * case defines the range of the heatmap from the source component. The
	 * PropertyType is used to correctly format the double value when displayed
	 * in the UI. E.g. when selecting PropertyType.Length the value "5.0" will
	 * be formatted as follows: "5.0 m".
	 */
	final DoubleParameter rpRangeParameter = params.createDouble("rpRange", PropertyType.LENGTH);

	/**
	 * The second DoubleParameter for the length of the step.
	 */
	final DoubleParameter rpStepParameter = params.createDouble("rpStep", PropertyType.LENGTH);

	/**
	 * Add the UI definition.
	 */
	private final HeatmapVisualizationRuleUIDefinition uiDefinition = new HeatmapVisualizationRuleUIDefinition(this);

	/**
	 * This method is called for every component that passes through the default filter
	 *
	 * @param component  the component to check
	 */
	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		/*
		 * A rule parameter value can be accessed by calling the corresponding
		 * method.
		 */
		double range = rpRangeParameter.getValue();
		ComponentFilter rangeFilter = rpEffectSourceFilter.getValue();

		/*
		 * Get the components within range by creating a filter that returns the components whose bounding boxes
		 * are within the given range of the checked component's axis-aligned bounding box.
		 */
		ComponentFilter componentsInRangeFilter = AABBIntersectionFilter.ofComponentBounds(component, range, 0.0)
			.and(rangeFilter);
		Collection<Component> componentsInRange = SMC.getModel().getComponents(componentsInRangeFilter);

		/*
		 * Discard the checking if the range-value doesn't meet the requirements
		 * or there are no effect sources in range.
		 */
		if (range <= 0.0 || componentsInRange.isEmpty()) {
			// No result produced
			return Collections.emptySet();
		}

		/*
		 * Create the bitmap that contains the heatmap.
		 */
		Bitmap bitmap = createBitmapVisualization(component, componentsInRange);

		/*
		 * Create the result.
		 */
		Result result = createResult(component, componentsInRange, bitmap, resultFactory);

		/*
		 * Return the one result created for this component.
		 */
		return Collections.singleton(result);
	}

	private Result createResult(Component component, Collection<Component> componentsInRange, Bitmap bitmap,
		ResultFactory resultFactory) {

		/*
		 * Create the name and description for this result.
		 */
		String resultName = createResultName(component);
		String resultDescription = createResultDescription(componentsInRange);

		/*
		 * Results are created with the result factory. At least a name and description are required to create a result.
		 * The result will be attached to the component we are now checking.
		 */

		return resultFactory
			.create(resultName, resultDescription)

		/*
		 * When creating a result it's important to remember to add the other involved components
		 * to the result so that those will be visible in the UI.
		 */
		.withInvolvedComponents(componentsInRange)

		/*
		 * A custom visualization for the result can be created.
		 */
		.withVisualization(visualization -> {
			/*
			 * Add the bitmap to the visualization of the result.
			 */
			visualization.addVisualizationItem(bitmap);

			/*
			 * Add the component with 75% transparency to the visualization of the
			 * result.
			 */
			visualization.addComponent(component, 0.75);

			/*
			 * Add the effect-sources with no (0%) transparency to the visualization
			 * of the result.
			 */
			visualization.addComponents(componentsInRange, 0.0);
		});
	}

	private String createResultName(Component component) {
		return component.getName();
	}

	private String createResultDescription(Collection<Component> componentsInRange) {
		/*
		 * getString() allows user to handle the localized string(s) mapped by
		 * the key(s).
		 */
		String descriptionHeadline = RuleResources.of(this).getString("resultDescription", componentsInRange.size());

		/*
		 * Create the result description that contains the unique identifiers of
		 * the components listed below the description headline. By using the html
		 * you can modify the outlook of the description.
		 */
		StringBuilder resultDescription = new StringBuilder(descriptionHeadline);
		final String htmlBrTag = "<br>";
		resultDescription.append(htmlBrTag);
		for (Component componentInRange : componentsInRange) {
			String displayName = componentInRange.getName();
			resultDescription.append(htmlBrTag);
			resultDescription.append(displayName);
		}

		return resultDescription.toString();
	}

	private Bitmap createBitmapVisualization(Component component, Collection<Component> componentsInRange) {
		/*
		 * Initialize the graphics based on the area of the component.
		 */
		Area area = component.getFootprint().getArea();
		MAABB2d boundingRectangle = area.getBoundingRectangle();
		int imageType = BufferedImage.TYPE_INT_ARGB;
		BufferedImage image = new BufferedImage(BITMAP_RESOLUTION, BITMAP_RESOLUTION, imageType);
		Graphics2D graphics = createImageGraphics(image, boundingRectangle);
		graphics.setClip(createComponentShape(component));

		/*
		 * Draw the heatmap and effect-source into 2D-graphics for each component
		 * in the range.
		 */
		for (Component entityInRange : componentsInRange) {
			Shape shape = createComponentShape(entityInRange);
			drawHeatmap2D(graphics, rpRangeParameter.getValue(), rpStepParameter.getValue(), shape);
			drawEffectSource2D(graphics, shape);
		}

		/*
		 * This method returns the minimum size axis-aligned bounding box of the
		 * component.
		 */
		AABB3d componentBounds = component.getBoundingBox();

		/*
		 * VisualizationItemFactory allows the user to create visualization
		 * items.
		 */
		MVector3d location = boundingRectangle.getCentroid().to3dVector();
		double zOffsetToAvoidOverlapping = 0.03;
		location.setZ(componentBounds.getLowerBound().getZ() + zOffsetToAvoidOverlapping);
		double bitmapWidth = boundingRectangle.getSizeX();
		double bitmapHeight = boundingRectangle.getSizeY();

		return Bitmap.create(image, Vector3d.UNIT_Z, Vector3d.UNIT_Y, location, bitmapWidth, bitmapHeight);
	}

	private static AffineTransform createWorldToImageTransformation(AABB2d boundingRectangle) {
		AffineTransform affineTransform = new AffineTransform();
		double scaleX = BITMAP_RESOLUTION / boundingRectangle.getSizeX();
		double scaleY = BITMAP_RESOLUTION / boundingRectangle.getSizeY();
		affineTransform.scale(scaleX, scaleY);
		Vector2d offset = boundingRectangle.getLowerBound();
		double offsetX = -offset.getX();
		double offsetY = -offset.getY();
		affineTransform.translate(offsetX, offsetY);

		return affineTransform;
	}

	private static Graphics2D createImageGraphics(BufferedImage image, MAABB2d boundingRectangle) {
		AffineTransform affineTransform = createWorldToImageTransformation(boundingRectangle);
		Graphics2D graphics = image.createGraphics();
		graphics.setTransform(affineTransform);
		graphics.setColor(VISUALIZATION_COLOR);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		return graphics;
	}

	private static void drawHeatmap2D(Graphics2D imageGraphics, double range, double step, Shape shape) {
		boolean drawSteps = step < range && step > 0 && (range / step) < STEP_LIMIT;
		int stepsCount = drawSteps ? (int) Math.floor(range / step) : 0;
		float alpha = drawSteps ? 1f / (stepsCount + 1) : 0.25f;
		setAlphaComposite(imageGraphics, alpha);

		/*
		 * Draw heatmap from the edge of the source component to the distance of
		 * given range.
		 */
		drawShapeWithOffset(imageGraphics, shape, (float) range);

		/*
		 * Draw the heatmap steps if needed. Draw the steps from the edge of the
		 * source component. Steps divides the drawn heatmap range area to
		 * multiple slices (steps).
		 */
		if (drawSteps) {
			for (int i = 1; i <= stepsCount; i++) {
				float offset = (float) step * i;
				drawShapeWithOffset(imageGraphics, shape, offset);
			}
		}
	}

	private static void drawEffectSource2D(Graphics2D graphics, Shape shape) {
		setAlphaComposite(graphics, 1f);
		graphics.fill(shape);
		drawShapeWithOffset(graphics, shape, 0.0f);
	}

	private static void setAlphaComposite(Graphics2D graphics, float alpha) {
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	}

	private static void drawShapeWithOffset(Graphics2D graphics, Shape shape, float offset) {
		/*
		 * Increase the size of the shape with amount of offset-value. This is
		 * done by increasing the line thickness. The line thickness is twice
		 * the offset because it effects on both sides from the line.
		 */
		float edgeThickness = 2.0f * offset;
		graphics.setStroke(new BasicStroke(edgeThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
			0.0f, null, 0.0f));
		graphics.draw(shape);
	}

	private Shape createComponentShape(Component component) {

		/*
		 * Get the outline polygon of the footprint because we just need to have
		 * the outer polygon of the footprint.
		 */
		Polygon2d footprint = component.getFootprint().getOutline();

		/*
		 * Create the GeneralPath (java.awt.geom.Shape) of the footprint.
		 */
		GeneralPath path = new GeneralPath();
		List<Vector2d> vertices = footprint.getVertices();
		for (int i = 0; i < vertices.size(); i++) {
			Vector2d point = vertices.get(i);
			if (i == 0) {
				path.moveTo(point.getX(), point.getY());
			} else {
				path.lineTo(point.getX(), point.getY());
			}
		}
		path.closePath();

		return path;
	}

	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}
}
