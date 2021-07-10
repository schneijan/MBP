package de.ipvs.as.mbp.domain.access_control;

/**
 * Enumeration for all {@link ACAccess} types. 
 * 
 * @author Jakob Benz
 */
public enum ACAccessType {
	
	/**
	 * Read, e.g., a device.
	 */
	READ,
	
	/**
	 * Read, e.g., sensor value logs.
	 */
	READ_VALUE_LOGS,
	
	/**
	 * Read, e.g., sensor value logs statistics.
	 */
	READ_VALUE_LOG_STATS,
	
	/**
	 * Delete, e.g., sensor value logs.
	 */
	DELETE_VALUE_LOGS,
	
	/**
	 * Update an existing, e.g., rule.
	 */
	UPDATE,
	
	/**
	 * Delete an existing, e.g., device.
	 */
	DELETE,
	
	/**
	 * Monitor an existing, e.g., device.
	 */
	MONITOR,
	
	/**
	 * Deploy, e.g., an actuator. 
	 */
	DEPLOY,
	
	/**
	 * Undeploy, e.g., a sensor.
	 */
	UNDEPLOY,
	
	/**
	 * Start, e.g., monitoring.
	 */
	START,
	
	/**
	 * Stop, e.g., monitoring.
	 */
	STOP;
	
}
