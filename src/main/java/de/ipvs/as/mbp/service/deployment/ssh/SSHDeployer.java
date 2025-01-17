package de.ipvs.as.mbp.service.deployment.ssh;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Code;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.settings.BrokerLocation;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.error.DeploymentException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.service.NetworkService;
import de.ipvs.as.mbp.service.deployment.ComponentState;
import de.ipvs.as.mbp.service.deployment.DeviceState;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.settings.SettingsService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This component provides features for deploying components onto a remote
 * device. Furthermore it is possible to check the current state of a deployed
 * component and to undeploy the component again.
 */
@org.springframework.stereotype.Component
public class SSHDeployer implements IDeployer {
    // Deployment location on remote devices
    private static final String DEPLOY_DIR = "$HOME/scripts";
    private static final String DEPLOY_DIR_PREFIX = "mbp";

    // Names of the adapter scripts
    private static final String INSTALL_SCRIPT_NAME = "install.sh";
    private static final String START_SCRIPT_NAME = "start.sh";
    private static final String RUN_SCRIPT_NAME = "running.sh";
    private static final String STOP_SCRIPT_NAME = "stop.sh";
    private static final String MBP_CLIENT_PROPERTIES_FILE_NAME = "mbp.properties";

    // Timeout for availability checks (ms)
    private static final int AVAILABILITY_CHECK_TIMEOUT = 5000;

    // Class internal logger
    private static final Logger LOGGER = Logger.getLogger(SSHDeployer.class.getName());

    @Autowired
    private SSHSessionPool sshSessionPool;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private SettingsService settingsService;

    /**
     * Retrieves the current deployment state of a given component.
     *
     * @param component The component to retrieve the deployment state for
     * @return The current deployment state of the component
     */
    public ComponentState retrieveComponentState(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        // Get dedicated device of the component
        Device device = component.getDevice();

        // Determine availability state of the device
        DeviceState deviceState = retrieveDeviceState(device);

        // Check if device is available
        if (deviceState != DeviceState.SSH_AVAILABLE) {
            return ComponentState.NOT_READY;
        }

        try {
            // Check if component is not deployed
            if (!isComponentDeployed(component)) {
                return ComponentState.READY;
            }

            // Return matching state based on the result
            if (isComponentRunning(component)) {
                return ComponentState.RUNNING;
            } else {
                return ComponentState.DEPLOYED;
            }
        } catch (Exception e) {
            return ComponentState.UNKNOWN;
        }
    }

    /**
     * Retrieves the current availability state of a given device.
     *
     * @param device The device to retrieve the availability state for
     * @return The current availability state of the device
     */
    public DeviceState retrieveDeviceState(Device device) {
        // Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        // Get ip address
        String ipAddress = device.getIpAddress();

        // Check if remote device can be pinged
        boolean pingable = false;
        try {
            pingable = InetAddress.getByName(ipAddress).isReachable(AVAILABILITY_CHECK_TIMEOUT);
        } catch (IOException ignored) {
            // No abort, because pings could be just disabled in the network --> check SSH
        }

        // Check if it is possible to establish a SSH connection
        SSHSession sshSession;
        try {
            // Get new SSH session from pool
            sshSession = sshSessionPool.getNewSSHSession(device);
        } catch (IOException e) {
            sshSession = null;
        }

        // Check for valid SSH session object
        if (sshSession == null) {
            // Invalid, device is either online with no SSH or offline
            return pingable ? DeviceState.ONLINE : DeviceState.OFFLINE;
        }

        // Check if it is possible to execute a basic command
        if (sshSession.isCommandExecutable()) {
            return DeviceState.SSH_AVAILABLE;
        } else {
            // No commands can be executed via SSH, device is either online with no SSH or
            // offline
            return pingable ? DeviceState.ONLINE : DeviceState.OFFLINE;
        }
    }

    /**
     * Starts a component on its corresponding remote device and passes deployment
     * parameters to it.
     *
     * @param component             The component to start
     * @param parameterInstanceList List of parameter instances to pass
     */
    public void startComponent(Component component, List<ParameterInstance> parameterInstanceList) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.INFO, "Start request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        // Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        // Retrieve adapter
        Operator operator = component.getOperator();

        // Sanity check
        if (operator == null) {
            throw new IllegalArgumentException("Adapter must not be null.");
        }

        // Get dedicated device of the component
        Device device = component.getDevice();

        try {
            // Get SSH session from pool
            SSHSession sshSession = sshSessionPool.getSSHSession(device);

            // Create JSON string from parameters
            JSONArray parameterArray = convertParametersToJSON(operator, parameterInstanceList);
            String jsonString = convertJSONToCmdLineString(parameterArray);

            // Execute start script with parameters
            sshSession.executeShellScript(deploymentPath + "/" + START_SCRIPT_NAME, deploymentPath, jsonString);
            LOGGER.log(Level.INFO, "JsonString: " + jsonString);
        } catch (IOException e) {
            throw new DeploymentException("Starting failed due to a SSH error.");
        }

        LOGGER.log(Level.INFO, "Start was successful");
    }

    /**
     * Stops a component on its corresponding remote device.
     *
     * @param component The component to stop
     */
    public void stopComponent(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.INFO, "Stop request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        // Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        // Get dedicated device of the component
        Device device = component.getDevice();

        try {
            // Get SSH session from pool
            SSHSession sshSession = sshSessionPool.getSSHSession(device);

            // Execute start script with parameters
            sshSession.executeShellScript(deploymentPath + "/" + STOP_SCRIPT_NAME);
        } catch (IOException e) {
            throw new DeploymentException("Stopping failed due to a SSH error.");
        }

        LOGGER.log(Level.INFO, "Stop was successful");
    }

    /**
     * Deploys a component onto its corresponding remote device.
     *
     * @param component The component to deploy
     */
    public void deployComponent(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.INFO, "Deploy request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        // Retrieve adapter
        Operator operator = component.getOperator();

        // Sanity check
        if (operator == null) {
            throw new IllegalArgumentException("Operator must not be null.");
        }

        // Get dedicated device of the component
        Device device = component.getDevice();

        try {
            // Get SSH session from pool
            SSHSession sshSession = sshSessionPool.getSSHSession(device);

            // Resolve deployment path
            String deploymentPath = getDeploymentPath(component);

            // Create deployment directory
            LOGGER.log(Level.INFO, "Deploying to directory {1} ...", new Object[]{deploymentPath});
            sshSession.createDir(deploymentPath);
            LOGGER.log(Level.INFO, "Created directory successfully");

            LOGGER.log(Level.INFO, "Copying adapter files to target device....");

            // Iterate over all adapter files and copy them
            for (Code file : operator.getRoutines()) {
                // Check whether content is encoded as base64
                if (file.isBase64Encoded()) {
                    // Create file from base64
                    sshSession.createFileFromBase64(deploymentPath, file.getName(), file.getContent());
                } else {
                    // No base64 string, just copy file
                    sshSession.createFile(deploymentPath, file.getName(), file.getContent());
                }

                // Generate hash of the newly created file
                String fileHash = sshSession.generateHashOfFile(deploymentPath + "/" + file.getName());

                // Compare generated hash to the given one
                String givenHash = file.getHash();
                if ((fileHash != null) && (givenHash != null) && (!givenHash.isEmpty()) && (!fileHash.equals(givenHash))) {
                    throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Hash of copied file " + file.getName() + " does not match.");
                }
            }
            LOGGER.log(Level.INFO, "Copying adapter files was successful");

            // Resolve own IP address that might be used as broker IP address
            String brokerIP = networkService.getOwnIPAddress();

            // Determine from settings if a remote broker should be used
            Settings settings = settingsService.getSettings();
            if (settings.getBrokerLocation().equals(BrokerLocation.REMOTE)) {
                // Retrieve IP address of external broker from settings
                brokerIP = settings.getBrokerIPAddress();
            }

            // Sanity check
            if (brokerIP == null) {
                throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to resolve IP address of the broker.");
            }

            // Get topic name for the component
            String topicName = component.getTopicName();

            // Create .properties file on device
            String mbpProperties = createMBPProperties(component, brokerIP);
            sshSession.createFile(deploymentPath, MBP_CLIENT_PROPERTIES_FILE_NAME, mbpProperties);
            LOGGER.log(Level.INFO, "Creation of .properties file was successful");

            // Execute install script
            sshSession.changeFilePermissions(deploymentPath + "/" + INSTALL_SCRIPT_NAME, "+x");
            sshSession.executeShellScript(deploymentPath + "/" + INSTALL_SCRIPT_NAME, topicName, brokerIP, deploymentPath);

            LOGGER.log(Level.INFO, "Installation was successful");

            // Set permissions of other shell scripts
            sshSession.changeFilePermissions(deploymentPath + "/" + START_SCRIPT_NAME, "u+rwx");
            sshSession.changeFilePermissions(deploymentPath + "/" + RUN_SCRIPT_NAME, "+x");
            sshSession.changeFilePermissions(deploymentPath + "/" + STOP_SCRIPT_NAME, "+x");
        } catch (IOException e) {
            throw new DeploymentException("Deployment failed due to a SSH error.");
        }

        LOGGER.log(Level.INFO, "Deployment was successful");
    }

    /**
     * Undeploys a component from its corresponding remote device.
     *
     * @param component The component to undeploy
     */
    public void undeployComponent(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.INFO, "Undeploy request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        // Get dedicated device of the component
        Device device = component.getDevice();

        try {
            // Get SSH session from pool
            SSHSession sshSession = sshSessionPool.getSSHSession(device);

            // Resolve deployment path
            String deploymentPath = getDeploymentPath(component);

            // Try to execute stop script in order to terminate the execution of the component
            try {
                sshSession.executeShellScript(deploymentPath + "/" + STOP_SCRIPT_NAME);
            } catch (Exception ignored) {
                // Just catch. Undeployment should be always possible
            }

            // Remove deployment directory from remote machine
            sshSession.removeDir(deploymentPath);
        } catch (IOException e) {
            throw new DeploymentException("Undeployment failed due to a SSH error.");
        }
    }

    /**
     * Undeploys a component from its corresponding remote device if it is currently running.
     *
     * @param component The component to undeploy
     */
    public void undeployIfRunning(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        // Determine component state
        ComponentState componentState = retrieveComponentState(component);

        // Undeploy component if running
        if (ComponentState.RUNNING.equals(componentState)) {
            this.undeployComponent(component);
        }
    }

    /**
     * Checks whether a given component is currently running on its corresponding remote device.
     *
     * @param component The component to check
     * @return True, if the component is running; false otherwise
     */
    public boolean isComponentRunning(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.INFO, "Checking status of component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        // Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        // Get dedicated device of the component
        Device device = component.getDevice();

        try {
            // Get SSH session from pool
            SSHSession sshSession = sshSessionPool.getSSHSession(device);

            // Reset output stream of session
            sshSession.resetStdOutStream();

            // Get output stream of the session
            OutputStream stdOutStream = sshSession.getStdOutStream();

            // Execute run script to check whether the adapter is running
            sshSession.executeShellScript(deploymentPath + "/" + RUN_SCRIPT_NAME);

            // Get return value of script
            String returnValue = stdOutStream.toString().toLowerCase();
            return returnValue.contains("true");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            LOGGER.log(Level.INFO, "Operator has not been deployed yet or is not running");
        }

        return false;
    }

    /**
     * Checks whether a given component is currently deployed on its corresponding remote device.
     *
     * @param component The component to check
     * @return True, if the component is deployed; false otherwise
     */
    public boolean isComponentDeployed(Component component) {
        // Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        try {
            // Get SSH session from pool
            SSHSession sshSession = sshSessionPool.getSSHSession(component.getDevice());

            // Resolve deployment path
            String deploymentPath = getDeploymentPath(component);

            // Check if deployment folder exists
            return sshSession.dirExists(deploymentPath);
        } catch (IOException e) {
            throw new DeploymentException("Deployment state cannot be determined due to an SSH error.");
        }
    }

    /**
     * Converts a JSON array into a string that might be passed as command line
     * parameter.
     *
     * @param jsonArray The JSON array to convert
     * @return The corresponding JSON string (command line compatible)
     */
    private String convertJSONToCmdLineString(JSONArray jsonArray) { // Resource<
        String jsonString = jsonArray.toString();
        // Escape backslashes
        jsonString = jsonString.replace("\"", "\\\"");

        // Wrap string with double quotes
        jsonString = "\"" + jsonString + "\"";

        return jsonString;
    }

    /**
     * Converts a list of parameter instances into a JSON array.
     *
     * @param operator              The adapter that specifies the parameters
     * @param parameterInstanceList A list of parameter instances that correspond to
     *                              the adapter parameters
     * @return A JSON array that contains the parameter instances
     */
    private JSONArray convertParametersToJSON(Operator operator, List<ParameterInstance> parameterInstanceList) {
        JSONArray parameterArray = new JSONArray();

        // Sanity check
        if (operator == null) {
            throw new IllegalArgumentException("Adapter must not be null.");
        } else if ((parameterInstanceList == null) || parameterInstanceList.isEmpty()) {
            // Return empty array
            return parameterArray;
        }

        // Get specified parameters from adapter
        List<Parameter> parameters = operator.getParameters();

        // Iterate over all specified parameters
        for (Parameter parameter : parameters) {
            boolean matchingFound = false;
            // Iterate over all parameter instances
            for (ParameterInstance parameterInstance : parameterInstanceList) {
                // Find matching instance for this parameter
                if (!parameter.isInstanceValid(parameterInstance)) {
                    continue;
                }
                matchingFound = true;

                // Create JSON object for the parameter that can be added to the parameter array
                JSONObject parameterObject = new JSONObject();
                try {
                    // Add properties to object
                    parameterObject.put("name", parameter.getName());
                    parameterObject.put("value", parameterInstance.getValue());
                } catch (JSONException ignored) {
                }
                // Add object to parameter
                parameterArray.put(parameterObject);
            }

            // Throw exception if no valid instance was provided for a mandatory parameter
            if ((!matchingFound) && parameter.isMandatory()) {
                throw new IllegalArgumentException(
                        "No valid instance for parameter \"" + parameter.getName() + "\" provided.");
            }
        }

        return parameterArray;
    }

    /**
     * Creates the content of a properties that is created onto devices upon
     * operator deployment.
     *
     * @param component  The component to create the content
     * @param brokerHost The MQTT broker IP of the MBP
     * @return The content of the properties file
     */
    private String createMBPProperties(Component component, String brokerHost) {
        String separator = System.lineSeparator();
        StringBuilder propertiesString = new StringBuilder("[MBP]" + separator);
        propertiesString.append("brokerHost=").append(brokerHost).append(separator);
        propertiesString.append("brokerPort=1883").append(separator);
        propertiesString.append("brokerTopic=").append(component.getComponentTypeName()).append("/").append(component.getId()).append(separator);
        propertiesString.append("brokerActionTopic=action/").append(component.getId()).append("/#").append(separator);
        propertiesString.append(separator);
        propertiesString.append("[Component]").append(separator);
        propertiesString.append("componentId=").append(component.getId()).append(separator);

        return propertiesString.toString();
    }

    /**
     * Returns the path to the directory to which the component is deployed.
     *
     * @param component The component
     * @return The path to the deployment directory
     */
    private static String getDeploymentPath(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("The component must not be null.");
        }
        return DEPLOY_DIR + "/" + DEPLOY_DIR_PREFIX + component.getId();
    }
}
