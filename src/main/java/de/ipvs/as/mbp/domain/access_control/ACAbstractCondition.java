package de.ipvs.as.mbp.domain.access_control;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.access_control.jquerybuilder.JQBOutput;
import de.ipvs.as.mbp.domain.access_control.jquerybuilder.JQBRule;
import de.ipvs.as.mbp.service.access_control.ACAbstractConditionEvaluator;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract base class for all access-control policy conditions.
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractCondition extends ACAbstractEntity {
	
	@Transient
	private String humanReadableDescription;
	
	/**
	 * No-args constructor.
	 */
	public ACAbstractCondition() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this condition.
	 * @param description the description of this condition.
	 * @param ownerId the id of the {@link User} that owns this policy.
	 */
	public ACAbstractCondition(String name, String description, String ownerId) {
		super(name, description, ownerId);
	}
	
	// - - -
	
	public String getHumanReadableDescription() {
		return humanReadableDescription;
	}
	
	public ACAbstractCondition computeAndSetHumanReadableDescription() {
		humanReadableDescription = toHumanReadableString();
		return this;
	}
	
	// - - -
	
	@JsonIgnore
	public abstract String toHumanReadableString();
	
	/**
	 * Evaluates this condition for a specific access request.
	 * 
	 * @param access the {@link ACAccess}.
	 * @param request the {@link ACAccessRequest}.
	 * @return the result of the condition evaluation.
	 * @throws ACConditionEvaluatorNotAvailableException 
	 */
	public boolean evaluate(ACAccess access, ACAccessRequest request) throws ACConditionEvaluatorNotAvailableException {
		return findEvaluator().evaluate(this, access, request);
	}
	
	/**
	 * Performs a lookup for the corresponding condition evaluator.
	 * 
	 * @return the {@link ACAbstractConditionEvaluator}.
	 * @throws ACConditionEvaluatorNotAvailableException
	 */
	ACAbstractConditionEvaluator<ACAbstractCondition> findEvaluator() throws ACConditionEvaluatorNotAvailableException {
		// Check whether the ACEvaluate annotation has been specified for the condition to evaluate
		if (!getClass().isAnnotationPresent(ACEvaluate.class)) {
			throw new MissingAnnotationException(ACEvaluate.class, getClass().getName());
		}
		
		// Lookup ACEvalute annotation
		ACEvaluate evaluateAnnotation = getClass().getAnnotation(ACEvaluate.class);
		
		// Lookup the condition evaluator class
		@SuppressWarnings("unchecked")
		Class<? extends ACAbstractConditionEvaluator<ACAbstractCondition>> evaluatorClass = (Class<? extends ACAbstractConditionEvaluator<ACAbstractCondition>>) evaluateAnnotation.using();
		
		// Create and return new instance of the evaluator class
		try {
			return evaluatorClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new ACConditionEvaluatorNotAvailableException(e.getMessage(), e);
		}
	}

	// - - -
	
	public static ACAbstractCondition forJQBOutput(String output) throws JsonMappingException, JsonProcessingException {
		JQBOutput jqbOutput = new ObjectMapper().readValue(output, JQBOutput.class);
		if (jqbOutput.getRules().size() == 1) {
			// Only one simple condition
			return ACSimpleCondition.forJQBRule((JQBRule) jqbOutput.getRules().get(0));
		} else {
			return ACCompositeCondition.forJQBRuleGroup(jqbOutput);
		}
	}
	
}
