package de.ipvs.as.mbp.domain.access_control;

import java.util.HashMap;
import java.util.Map;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;

/**
 * Abstract base class for access-control effects, that, e.g., filter or modify
 * requested (sensor) data.
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractEffect extends ACAbstractEntity {
	
	/**
	 * The list of parameters this effect requires to be applied.
	 */
	private Map<String, String> parameters = new HashMap<>();
	
	/**
	 * The {@link ACEffectType} of this effect.
	 */
	private final ACEffectType type = getClass().getAnnotation(ACEffect.class).type();
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACAbstractEffect() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this effect.
	 * @param description the description of this effect.
	 * @param parameters the list of parameters this required to be applied.
	 * @param ownerId the id of the {@link User} that owns this policy.
	 */
	public ACAbstractEffect(String name, String description, Map<String, String> parameters, String ownerId) {
		super(name, description, ownerId);
	}
	
	// - - -
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public ACAbstractEffect setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;
	}
	
	public ACEffectType getType() {
		return type;
	}
	
	// - - -

	public abstract ValueLog apply(ValueLog valueLog);
	
	// - - -
	
	public static ACAbstractEffect forType(String type) throws InstantiationException, IllegalAccessException {
		return ACEffectType.valueOf(type).getClazz().newInstance();
	}
	
	public static ACAbstractEffect forType(ACEffectType type) throws InstantiationException, IllegalAccessException {
		return type.getClazz().newInstance();
	}
	
}
