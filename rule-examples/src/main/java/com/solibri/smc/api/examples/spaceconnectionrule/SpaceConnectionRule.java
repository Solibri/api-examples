package com.solibri.smc.api.examples.spaceconnectionrule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.solibri.smc.api.checking.ComponentSelector;
import com.solibri.smc.api.checking.ConcurrentRule;
import com.solibri.smc.api.checking.EnumerationParameter;
import com.solibri.smc.api.checking.FilterParameter;
import com.solibri.smc.api.checking.PreCheckResult;
import com.solibri.smc.api.checking.Result;
import com.solibri.smc.api.checking.ResultFactory;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.model.Model;
import com.solibri.smc.api.model.components.Space;
import com.solibri.smc.api.ui.UIContainer;

public final class SpaceConnectionRule extends ConcurrentRule {

	static final String DIRECT_ACCESS_ALLOWED = "rpDirectAccessCondition.ALLOWED";

	static final String DIRECT_ACCESS_REQUIRED = "rpDirectAccessCondition.REQUIRED";

	static final String DIRECT_ACCESS_FORBIDDEN = "rpDirectAccessCondition.FORBIDDEN";

	static final String TYPE_ANY_DOOR_OR_OPENING = "rpTypeOfAccessCondition.ANY_DOOR_OR_OPENING";

	static final String TYPE_CONSIDER_DOORS = "rpTypeOfAccessCondition.CONSIDER_DOORS";

	static final String TYPE_CONSIDER_OPENINGS = "rpTypeOfAccessCondition.CONSIDER_OPENINGS";

	static final String EXIT_ALLOWED = "rpDirectAccessOutsideCondition.EXIT_ALLOWED";

	static final String EXIT_REQUIRED = "rpDirectAccessOutsideCondition.EXIT_REQUIRED";

	static final String EXIT_FORBIDDEN = "rpDirectAccessOutsideCondition.EXIT_FORBIDDEN";

	private final RuleParameters params = RuleParameters.of(this);

	final FilterParameter rpSpacesFilterA = this.getDefaultFilterParameter();
	final FilterParameter rpSpacesFilterB = params.createFilter("rpSpacesFilterB");

	static List<String> rpDirectAccessConditionParametersList = Arrays.asList(
		DIRECT_ACCESS_ALLOWED,
		DIRECT_ACCESS_REQUIRED,
		DIRECT_ACCESS_FORBIDDEN);
	final EnumerationParameter rpDirectAccessCondition = params
		.createEnumeration("rpDirectAccessCondition", rpDirectAccessConditionParametersList);

	static List<String> rpTypeOfAccessConditionParametersList = Arrays.asList(
		TYPE_ANY_DOOR_OR_OPENING,
		TYPE_CONSIDER_DOORS,
		TYPE_CONSIDER_OPENINGS);

	final EnumerationParameter rpTypeOfAccessCondition = params
		.createEnumeration("rpTypeOfAccessCondition", rpTypeOfAccessConditionParametersList);

	static List<String> rpDirectAccessOutsideConditionParametersList = Arrays.asList(
		EXIT_ALLOWED,
		EXIT_REQUIRED,
		EXIT_FORBIDDEN);
	final EnumerationParameter rpDirectAccessOutsideCondition = params
		.createEnumeration("rpDirectAccessOutsideCondition",
			rpDirectAccessOutsideConditionParametersList);

	final EnumerationParameter rpTypeOfAccessOutsideCondition = params
		.createEnumeration("rpTypeOfAccessOutsideCondition",
			rpTypeOfAccessConditionParametersList);

	private final SpaceConnectionRuleUIDefinition uiDefinition = new SpaceConnectionRuleUIDefinition(this);

	private Model targetModel;

	Model getTargetModel() {
		return targetModel;
	}

	@Override
	public PreCheckResult preCheck(ComponentSelector components) {
		targetModel = components.getTargetModel();
		return super.preCheck(components);
	}

	@Override
	public Collection<Result> check(Component component, ResultFactory resultFactory) {
		List<Result> results = new ArrayList<>();

		if (!(component instanceof Space)) {
			return Collections.emptyList();
		}
		Space spaceA = (Space) component;

		results.addAll(checkSpaceToSpaceConnection(spaceA, resultFactory));
		results.addAll(checkSpaceToOutsideAccess(spaceA, resultFactory));

		return results;
	}

	@Override
	public UIContainer getParametersUIDefinition() {
		return uiDefinition.getDefinitionContainer();
	}

	private Collection<Result> checkSpaceToSpaceConnection(Space entity, ResultFactory resultFactory) {
		SpaceToSpaceConnectionChecking accessChecking = new SpaceToSpaceConnectionChecking(this);
		return accessChecking.checkDirectSpaceToSpaceConnection(entity, resultFactory);
	}

	private Collection<Result> checkSpaceToOutsideAccess(Space entity, ResultFactory resultFactory) {
		SpaceToOutsideChecking spaceToOutsideChecking = new SpaceToOutsideChecking(this);
		return spaceToOutsideChecking.checkSpaceToOutsideAccess(entity, resultFactory);
	}

	@Override
	public Map<String, String> getParameterTemplateKeyToIdMap() {
		Map<String, String> parameterTemplateKey = new HashMap<>();
		parameterTemplateKey.put("PARAM_SPACES_A_TO_CHECK", "rpComponentFilter");
		parameterTemplateKey.put("PARAM_SPACES_B_TO_CHECK", "rpSpacesFilterB");
		parameterTemplateKey.put("PARAM_DIRECT_ACCESS_TO_SPACE_B", "rpDirectAccessCondition");
		parameterTemplateKey.put("PARAM_TYPE_OF_DIRECT_ACCESS_TO_CONSIDER_SPACE_B", "rpTypeOfAccessCondition");
		parameterTemplateKey.put("PARAM_DIRECT_EXIT_TO_OUTSIDE_FROM_SPACE_A", "rpDirectAccessOutsideCondition");
		parameterTemplateKey.put("PARAM_TYPE_OF_DIRECT_ACCESS_TO_CONSIDER_SPACE_A", "rpTypeOfAccessOutsideCondition");
		return parameterTemplateKey;
	}
}
