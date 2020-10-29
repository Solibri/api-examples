package com.solibri.smc.api.examples;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.ComponentType;
import com.solibri.smc.api.model.PropertyType;

/**
 * This example custom Information returns the installation schedule of the component.
 *
 * The information is defined only for wall, slabs and roofs.
 */
public class SchedulingInformation implements Information<Date> {

	private static final long ONE_DAY_TIME = 24L * 3600L * 1000L;

	private static Date START_DATE;

	static {
		try {
			START_DATE = new SimpleDateFormat("dd/MM/yyyy").parse("31/12/2020");
		} catch (ParseException e) {
			START_DATE = new Date();
		}
	}

	@Override
	public String getUniqueId() {
		return "Scheduling";
	}

	@Override
	public Optional<Date> getInformation(Component component) {

		// The information is defined only for wall, slabs and roofs.
		if (component.getComponentType() != ComponentType.WALL && component.getComponentType() != ComponentType.SLAB
			&& component.getComponentType() != ComponentType.ROOF) {
			return Optional.empty();
		}

		/*
		 * The real scheduling date could be fetched from external scheduling system. This example just estimates the
		 * date from bottom elevation of the component. One meter in elevation is scheduled one day later.
		 */
		double elevation = component.getGlobalBottomElevation();
		Date installationDate = new Date(START_DATE.getTime() + (long) elevation * ONE_DAY_TIME);
		return Optional.of(installationDate);
	}

	@Override
	public PropertyType getType() {
		return PropertyType.DATE;
	}

}
