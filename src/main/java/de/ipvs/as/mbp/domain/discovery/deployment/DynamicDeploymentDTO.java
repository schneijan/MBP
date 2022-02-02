package de.ipvs.as.mbp.domain.discovery.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * DTOs for {@link DynamicDeployment}s.
 */
public class DynamicDeploymentDTO {

    //Name of the dynamic deployment
    private String name;

    //Id of the operator to use
    @JsonProperty("operator")
    private String operatorId;

    //Id of the device template to use
    @JsonProperty("deviceTemplate")
    private String deviceTemplateId;

    /**
     * Creates a new, empty DTO for a {@link DynamicDeployment}.
     */
    public DynamicDeploymentDTO() {

    }

    /**
     * Returns the name of the dynamic deployment.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ID of the operator to use in the dynamic deployment.
     *
     * @return The operator ID
     */
    public String getOperatorId() {
        return operatorId;
    }

    /**
     * Returns the ID of the device template to use in the dynamic deployment.
     *
     * @return The device template ID
     */
    public String getDeviceTemplateId() {
        return deviceTemplateId;
    }
}