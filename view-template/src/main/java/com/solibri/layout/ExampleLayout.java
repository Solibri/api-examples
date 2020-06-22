package com.solibri.layout;

import java.util.ArrayList;
import java.util.List;

import com.solibri.smc.api.layout.Layout;
import com.solibri.smc.api.layout.LayoutView;
import com.solibri.smc.api.layout.Split;
import com.solibri.smc.api.ui.InternalViews;
import com.solibri.smc.api.ui.View;

/**
 * Defines an example layout that includes model tree view, 3D view and info view. Model tree view takes 1/3 of the
 * space in the left side of the layout. Info view and 3D view share the leftover space on top of each other on the
 * right side.
 */
public class ExampleLayout implements Layout {

	private static final Split FIRST_SPLIT = new FirstSplit();
	private static final Split MODEL_TREE_SPLIT = new ModelTreeSplit();
	private static final Split INFO_3D_SPLIT = new Info3DSplit();

	@Override
	public String getName() {
		return "Example";
	}

	@Override
	public List<Split> getSplits() {
		List<Split> splits = new ArrayList<>();
		splits.add(FIRST_SPLIT);
		return splits;
	}

	/**
	 * Splits the screen horizontally in two.
	 */
	private static class FirstSplit implements Split {

		@Override
		public Orientation getOrientation() {
			return Orientation.HORIZONTAL;
		}

		@Override
		public double getWeight() {
			// The only split in this stage, so can be anything, but 1.0 is simple.
			return 1.0;
		}

		@Override
		public List<Split> getSubSplits() {
			List<Split> splits = new ArrayList<>();
			splits.add(MODEL_TREE_SPLIT);
			splits.add(INFO_3D_SPLIT);
			return splits;
		}

		@Override
		public List<LayoutView> getViews() {
			// This stage has no views yet.
			return new ArrayList<>();
		}

	}

	private static class ModelTreeSplit implements Split {

		@Override
		public Orientation getOrientation() {
			// Could be anything since this only has one view in it.
			return Orientation.HORIZONTAL;
		}

		@Override
		public double getWeight() {
			return 0.33;
		}

		@Override
		public List<Split> getSubSplits() {
			// Has no subsplits.
			return new ArrayList<>();
		}

		@Override
		public List<LayoutView> getViews() {
			List<LayoutView> views = new ArrayList<>();
			views.add(MODEL_TREE_VIEW);
			return views;
		}

		private static final LayoutView MODEL_TREE_VIEW = new LayoutView() {

			@Override
			public double getWeight() {
				// Could be anything since this is the only view in its split.
				return 1.0;
			}

			@Override
			public View getView() {
				return InternalViews.MODEL_TREE_VIEW;
			}

			@Override
			public State getState() {
				// It is best to use DOCKED always.
				return State.DOCKED;
			}
		};

	}

	private static class Info3DSplit implements Split {

		@Override
		public Orientation getOrientation() {
			// Vertical makes the views be on top of each other.
			return Orientation.VERTICAL;
		}

		@Override
		public double getWeight() {
			return 0.67;
		}

		@Override
		public List<Split> getSubSplits() {
			// Has no subsplits.
			return new ArrayList<>();
		}

		@Override
		public List<LayoutView> getViews() {
			List<LayoutView> views = new ArrayList<>();
			views.add(INFO_VIEW);
			views.add(THREED_VIEW);
			return views;
		}

		private static final LayoutView INFO_VIEW = new LayoutView() {

			@Override
			public double getWeight() {
				// Info view takes 60 % of the space.
				return 0.4;
			}

			@Override
			public View getView() {
				return InternalViews.INFO_VIEW;
			}

			@Override
			public State getState() {
				// It is best to use DOCKED always.
				return State.DOCKED;
			}
		};

		private static final LayoutView THREED_VIEW = new LayoutView() {

			@Override
			public double getWeight() {
				// 3D takes 60 % of the space.
				return 0.6;
			}

			@Override
			public View getView() {
				return InternalViews.THREED_VIEW;
			}

			@Override
			public State getState() {
				// It is best to use DOCKED always.
				return State.DOCKED;
			}
		};

	}

}
