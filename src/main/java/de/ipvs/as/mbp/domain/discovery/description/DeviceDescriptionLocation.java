package de.ipvs.as.mbp.domain.discovery.description;

/**
 * Objects of this class represent the locations of devices within {@link DeviceDescription}s.
 */
public class DeviceDescriptionLocation {
    //Textual description of the location
    private String description;

    //Geographic location in lon/lat
    private DeviceDescriptionGeoPoint coordinates;

    /**
     * Creates a new, empty location object.
     */
    public DeviceDescriptionLocation() {

    }

    /**
     * Returns the informal description of the device's location.
     *
     * @return The informal description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the informal description of the device's location.
     *
     * @param description The description to set
     * @return The location object
     */
    public DeviceDescriptionLocation setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns the geographical location of the device.
     *
     * @return The geographical location
     */
    public DeviceDescriptionGeoPoint getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the geographical location of the device.
     *
     * @param coordinates The geographical location to set
     * @return The location object
     */
    public DeviceDescriptionLocation setCoordinates(DeviceDescriptionGeoPoint coordinates) {
        this.coordinates = coordinates;
        return this;
    }
}
