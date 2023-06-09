package com.solibri.smc.api.examples;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.info.Information;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.PropertyType;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

/**
 * This example custom Information uses dynamic Java code to compile a custom property on the fly. The algorithm used
 * can be defined as Java code and can be changed on-the-fly.
 *
 * Example code would be:
 *
 * {Double distance = (Double) $1.distance(com.solibri.geometry.linearalgebra.Vector3d.ZERO).get(); Double sqrt =
 * Double.valueOf(java.lang.Math.sqrt(distance.doubleValue())); return String.valueOf(sqrt);}
 *
 * This method definition calculates the distance of the given component from the origin point in the model.
 */
public class DynamicInformation implements Information<String> {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getUniqueId() {
		return "Dynamic information";
	}

	@Override
	public String getName() {
		return SMC.getSettings().getSetting(DynamicNameSetting.class).getValue();
	}

	@Override
	public Optional<String> getInformation(Component component) {
		String code = SMC.getSettings().getSetting(DynamicValueSetting.class).getValue();
		try {
			ClassPool pool = ClassPool.getDefault();
			CtClass componentClass = pool.get("com.solibri.smc.api.model.Component");
			CtClass cc = pool.makeClass("com.solibri.smc.api.examples.RuntimeDynamicInformation" + UUID.randomUUID());
			CtMethod newmethod = new CtMethod(pool.get(String.class.getCanonicalName()), "get", new CtClass[] {
				componentClass }, cc);
			newmethod.setBody(code);
			cc.addMethod(newmethod);
			cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
			cc.writeFile();

			Class clazz = cc.toClass();

			for (Method me : clazz.getDeclaredMethods()) {
				return Optional.ofNullable(String.valueOf(me.invoke(clazz.getDeclaredConstructor().newInstance(), component)));
			}

		} catch (Exception e) {
			LOG.error("Failed to compile the given syntax: {}", code, e);
		}
		return Optional.empty();
	}

	@Override
	public PropertyType getType() {
		return PropertyType.STRING;
	}

}
