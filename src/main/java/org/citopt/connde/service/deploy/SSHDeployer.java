package org.citopt.connde.service.deploy;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.Code;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This component provides features for deploying components onto a remote device. Furthermore it is possible
 * to check the current state of a deployed component and to undeploy the component again.
 *
 * @author rafaelkperes
 *         <p>
 *         Refactored by Jan on 03.12.2018.
 */
@org.springframework.stereotype.Component
public class SSHDeployer {

    private static final String INSTALL_SCRIPT_NAME = "install.sh";
    private static final String START_SCRIPT_NAME = "start.sh";
    private static final String RUN_SCRIPT_NAME = "running.sh";
    private static final String STOP_SCRIPT_NAME = "stop.sh";

    @Autowired
    private NetworkService networkService;

    private static final Logger LOGGER = Logger.getLogger(SSHDeployer.class.getName());

    private static final String DEPLOY_DIR = "~/scripts";
    private static final String DEPLOY_DIR_PREFIX = "connde";

    public static final int SSH_PORT = 22;

    private static final String REGEX_BASE64_PREFIX = "^data\\:[a-zA-Z0-9\\/,\\-]*\\;base64\\,";

    /**
     * Returns the path to the directory to which the component is deployed.
     *
     * @param component The component
     * @return The path to the deployment directory
     */
    public static String getDeploymentPath(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("The component must not be null.");
        }
        return DEPLOY_DIR + "/" + DEPLOY_DIR_PREFIX + component.getId();
    }

    /**
     * Deploys a component onto the dedicated remote device.
     *
     * @param component The component to deployComponent
     * @throws IOException In case of an I/O issue
     */
    public void deployComponent(Component component) throws IOException {
        //Validity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.FINE, "Deploy request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        //Establish new SSH session
        SSHSession sshSession = establishSSHConnection(component);

        //Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        //Create deployment directory
        LOGGER.log(Level.FINE, "Deploying to directory {1} ...", new Object[]{deploymentPath});
        sshSession.createDir(deploymentPath);
        LOGGER.log(Level.FINE, "Created directory successfully");

        //Retrieve adapter
        Adapter adapter = component.getAdapter();

        //Validity check
        if(adapter == null){
            throw new IllegalArgumentException("Adapter must not be null.");
        }

        LOGGER.log(Level.FINE, "Copying adapter files to target device....");

        //Iterate over all adapter files and copy them
        for (Code file : adapter.getRoutines()) {
            String fileContent = file.getContent();

            //Check whether content is encoded as base64
            if (fileContent.matches(REGEX_BASE64_PREFIX + ".+")) {
                //Remove base64 prefix
                fileContent = fileContent.replaceAll(REGEX_BASE64_PREFIX, "");

                //Create file from base64
                sshSession.createFileFromBase64(deploymentPath, file.getName(), fileContent);
            } else {
                // No base64 string, just copy file
                sshSession.createFile(deploymentPath, file.getName(), fileContent);
            }
        }
        LOGGER.log(Level.FINE, "Copying adapter files was succesful");

        //Retrieve ip address of the mqtt broker
        String brokerIP = networkService.getMQTTBrokerIP();

        //Get topic name for the component
        String topicName = component.getTopicName();

        //Execute install script
        sshSession.changeFilePermissions(deploymentPath + "/" + INSTALL_SCRIPT_NAME, "+x");
        sshSession.executeShellScript(deploymentPath + "/" + INSTALL_SCRIPT_NAME, topicName, brokerIP, deploymentPath);

        LOGGER.log(Level.FINE, "Installation was successful");

        //Execute start script
        sshSession.changeFilePermissions(deploymentPath + "/" + START_SCRIPT_NAME, "u+rwx");
        sshSession.executeShellScript(deploymentPath + "/" + START_SCRIPT_NAME, deploymentPath);

        LOGGER.log(Level.FINE, "Start was successful");

        //Set permissions of other shell scripts
        sshSession.changeFilePermissions(deploymentPath + "/" + RUN_SCRIPT_NAME, "+x");
        sshSession.changeFilePermissions(deploymentPath + "/" + STOP_SCRIPT_NAME, "+x");

        LOGGER.log(Level.FINE, "Deployment was successful");
    }

    /**
     * Checks whether the component is currently running on the dedicated remote device.
     *
     * @param component The component to check
     * @return True, if the component is running; false otherwise
     * @throws IOException In case of an I/O issue
     */
    public boolean isComponentRunning(Component component) throws IOException {
        //Validity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.FINE, "Checking status of component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        //Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        //Establish new SSH session
        SSHSession sshSession = establishSSHConnection(component);

        //Get output stream of the session
        OutputStream stdOutStream = sshSession.getStdOutStream();

        //Execute run script to check whether the adapter is running and catch exceptions
        try {
            sshSession.executeShellScript(deploymentPath + "/" + RUN_SCRIPT_NAME);

            //Get return value of script
            String returnValue = stdOutStream.toString().toLowerCase();
            return returnValue.contains("true");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Adapter has not been deployed yet or is not running");
            LOGGER.log(Level.WARNING, e.getClass().getCanonicalName() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Undeploys a component from the dedicated remote device.
     *
     * @param component The component to undeploy
     * @throws IOException In case of an I/O issue
     */
    public void undeployComponent(Component component) throws IOException {
        //Validity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        LOGGER.log(Level.FINE, "Undeploy request for component: " + "{0} (Type: {1})",
                new Object[]{component.getId(), component.getComponentTypeName()});

        //Establish new SSH session
        SSHSession sshSession = establishSSHConnection(component);

        //Resolve deployment path
        String deploymentPath = getDeploymentPath(component);

        //Execute stop script in order to termintate the execution of the component
        sshSession.executeShellScript(deploymentPath + "/" + STOP_SCRIPT_NAME);

        //Remove deployment directory from remote machine
        sshSession.removeDir(deploymentPath);
    }

    /*
    Establishes a SSH connection to the device that is referenced in the component object.
     */
    private SSHSession establishSSHConnection(Component component) throws UnknownHostException {
        //Validity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        //Retrieve device
        Device device = component.getDevice();

        //Validity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Retrieve private rsa key
        String rsaKey = device.getRsaKey();

        //Check key for validity
        if((rsaKey == null) || (rsaKey.isEmpty())){
            throw new IllegalArgumentException("No private RSA key for SSH connection provided.");
        }

        //Retrieve ssh connection parameter
        String url = device.getIpAddress();
        String username = device.getUsername();

        LOGGER.log(Level.FINE, "Establishing SSH connection to {0} (user: {1})",
                new Object[]{url, username});

        //Create new ssh session and connect
        SSHSession sshSession = new SSHSession(url, SSH_PORT, username, rsaKey);
        sshSession.connect();

        return sshSession;
    }
}