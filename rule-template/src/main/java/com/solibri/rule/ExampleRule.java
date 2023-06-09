package com.solibri.rule;

import java.util.Collection;
import java.util.Collections;

import com.solibri.smc.api.checking.OneByOneRule;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.StringParameter;
import com.solibri.smc.api.model.Component;

public final class ExampleRule extends OneByOneRule {
	
	private final RuleParameters params = RuleParameters.of(this);

	private final StringParameter stringParameter = params.createString("MyStringParameter");
	
	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		String stringParameterValue = stringParameter.getValue();
		Result result = resultFactory
			.create(stringParameterValue, "Description");
		return Collections.singleton(result);
	}
	
}
