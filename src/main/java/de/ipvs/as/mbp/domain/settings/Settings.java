package de.ipvs.as.mbp.domain.settings;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Collection of user-defined application-wide settings.
 */
@Document
public class Settings {
    //Fixed ID of the settings document within the repository
    public static final String SETTINGS_DOC_ID = "app_settings";

    @Id
    private String id = SETTINGS_DOC_ID;

    //All setting properties with default values
    private String senderName = "MBP";
    private BrokerLocation brokerLocation = BrokerLocation.LOCAL;
    private String brokerIPAddress = "127.0.0.1";
    private int brokerPort = 1883;
    private boolean demoMode = false;

    /**
     * Creates a new settings object with default values.
     */
    public Settings() {

    }

    /**
     * Returns the ID of the settings object (same for all).
     *
     * @return The fixed ID
     */
    protected String getId() {
        return id;
    }

    /**
     * Pretends to set the ID of the settings object. However, since the ID is fixed, this method does
     * effectively nothing, but is required to make the settings repository work properly.
     *
     * @param ignored The ID parameter without any effect
     */
    protected void setId(String ignored) {
        id = SETTINGS_DOC_ID;
    }

    /**
     * Returns the sender name that is displayed in messages that are published by the MBP in order to help
     * receivers of the messages to identify the sender.
     *
     * @return The sender name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Sets the sender name that is displayed in messages that are published by the MBP in order to help
     * receivers of the messages to identify the sender.
     *
     * @param senderName The sender name to set
     */
    public void setSenderName(String senderName) {
        //Sanity check
        if ((senderName == null) || (senderName.isEmpty())) {
            throw new IllegalArgumentException("The sender name must not be null or empty.");
        }

        //Set sender name
        this.senderName = senderName;
    }

    /**
     * Returns the location of the messaging broker that is supposed to be used for publish-subscribe-based messaging
     * within the MBP.
     *
     * @return The broker location
     */
    public BrokerLocation getBrokerLocation() {
        return brokerLocation;
    }

    /**
     * Sets the location of the messaging broker that is supposed to be used for publish-subscribe-based messaging
     * within the MBP.
     *
     * @param brokerLocation The broker location to set
     */
    public void setBrokerLocation(BrokerLocation brokerLocation) {
        //Sanity check
        if (brokerLocation == null) {
            throw new IllegalArgumentException("Broker location must not be null.");
        }
        this.brokerLocation = brokerLocation;
    }

    /**
     * Returns the IP address of the messaging broker that is supposed to be used for publish-subscribe-based
     * messaging within the MBP.
     *
     * @return The IP address of the broker
     */
    public String getBrokerIPAddress() {
        return brokerIPAddress;
    }

    /**
     * Sets the IP address of the messaging broker that is supposed to be used for publish-subscribe-based messaging
     * within the MBP. Only required if the broker location is "remote".
     *
     * @param brokerIPAddress The IP address of the broker to set
     */
    public void setBrokerIPAddress(String brokerIPAddress) {
        //Sanity check
        if ((brokerIPAddress == null) || brokerIPAddress.isEmpty()) {
            throw new IllegalArgumentException("Broker IP address must not be null or empty.");
        }

        this.brokerIPAddress = brokerIPAddress;
    }

    /**
     * Returns the port of the messaging broker that is supposed to be used for publish-subscribe-based messaging
     * within the MBP.
     *
     * @return The broker port
     */
    public int getBrokerPort() {
        return brokerPort;
    }

    /**
     * Sets the port of the messaging broker that is supposed to be used for publish-subscribe-based messaging
     * within the MBP.
     *
     * @param brokerPort The broker port to set
     */
    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }

    /**
     * Returns whether the demonstration mode is currently active.
     *
     * @return True, if the demonstration mode is active; false otherwise
     */
    public boolean isDemoMode() {
        return demoMode;
    }

    /**
     * Sets whether the demonstration mode is currently active.
     *
     * @param demoMode True, if the demonstration mode is active; false otherwise
     */
    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }
}
