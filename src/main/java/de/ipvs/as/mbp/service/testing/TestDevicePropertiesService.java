package de.ipvs.as.mbp.service.testing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * This service provides configuration values about test devices
 */
@Configuration
public class TestDevicePropertiesService {

    @Value("${testingTool.testDeviceName}")
    private String testDeviceName;

    @Value("${testingTool.ipAddressTestDevice}")
    private String ipAddressTestDevice;

    @Value("${testingTool.testDeviceUserName}")
    private String testDeviceUserName;

    @Value("${testingTool.testDevicePassword}")
    private String testDevicePassword;

    @Value("${testingTool.actuatorName}")
    private String actuatorName;

    @Value("${testingTool.testComponentIdentifier}")
    private String testComponentIdentifier;

    @Value("${testingTool.ConfigSensorNameKey}")
    private String configSensorNameKey;

    public String getTestDeviceName() {
        return testDeviceName;
    }

    public String getIpAddressTestDevice() {
        return ipAddressTestDevice;
    }

    public String getTestDeviceUserName() {
        return testDeviceUserName;
    }

    public String getTestDevicePassword() {
        return testDevicePassword;
    }

    public String getActuatorName() {
        return actuatorName;
    }

    public String getTestComponentIdentifier() {
        return testComponentIdentifier;
    }

    public String getConfigSensorNameKey() {
        return configSensorNameKey;
    }
}
