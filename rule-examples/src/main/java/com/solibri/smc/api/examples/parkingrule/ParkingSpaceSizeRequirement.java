package com.solibri.smc.api.examples.parkingrule;

import java.util.List;

import com.solibri.smc.api.checking.DoubleParameter;

final class ParkingSpaceSizeRequirement {

	private static final double ROUNDING_IN_MM_FACTOR = 1000;

	enum Requirement {
		MinimumWidth(false),
		MinimumLength(false),
		MinimumHeight(false),

		MaximumWidth(true),
		MaximumLength(true),
		MaximumHeight(true);

		private final boolean isMaximum;

		Requirement(boolean isMaximum) {
			this.isMaximum = isMaximum;
		}

		public boolean isMet(double limitValue, double actualValue) {
			return isMaximum ? actualValue <= limitValue : actualValue >= limitValue;
		}
	}

	static class Violation {

		private final Requirement requirement;
		private final double limitValue;
		private final double value;

		Violation(Requirement requirement, double limitValue, double value) {
			this.requirement = requirement;
			this.limitValue = limitValue;
			this.value = value;
		}

		Requirement getRequirement() {
			return requirement;
		}

		double getLimitValue() {
			return limitValue;
		}

		double getValue() {
			return value;
		}
	}

	void checkRequirement(Requirement requirement, DoubleParameter parameter, double actualValue,
						  List<Violation> violations) {
		//Round in mm
		double limitValue = Math.round(parameter.getValue() * ROUNDING_IN_MM_FACTOR);
		double roundedValue = Math.round(actualValue * ROUNDING_IN_MM_FACTOR);

		// Skip checking against parameters with zero as limit
		if (limitValue > 0.0 && !requirement.isMet(limitValue, roundedValue)) {
			Violation violation = new Violation(requirement, limitValue / ROUNDING_IN_MM_FACTOR,
				roundedValue / ROUNDING_IN_MM_FACTOR);
			violations.add(violation);
		}
	}
}
