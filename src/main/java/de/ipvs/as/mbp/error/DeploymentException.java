package de.ipvs.as.mbp.error;

import java.util.HashMap;
import java.util.Map;

public class DeploymentException extends RuntimeException {

	private Map<String, String> invalidParameters = new HashMap<>();

	public DeploymentException() {
		super();
	}
	
	public DeploymentException(String message) {
		super(message);
	}
	
	public DeploymentException(Map<String, String> invalidParameters) {
		super();
		this.invalidParameters = invalidParameters;
	}
	
	public DeploymentException(String message, Map<String, String> invalidParameters) {
		super(message);
		this.invalidParameters = invalidParameters;
	}

	public Map<String, String> getInvalidParameters() {
		return invalidParameters;
	}

	public DeploymentException addInvalidParameter(String key, String reason) {
		invalidParameters.put(key, reason);
		return this;
	}
	
}
