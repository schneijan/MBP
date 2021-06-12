package de.ipvs.as.mbp.domain.discovery.location;

import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import org.springframework.data.mongodb.core.mapping.Document;

@MBPEntity(createValidator = CircleLocationTemplateCreateValidator.class)
public class CircleLocationTemplate extends LocationTemplate {
    private double latitude;
    private double longitude;
    private double radius;

    public CircleLocationTemplate() {

    }

    public double getLatitude() {
        return latitude;
    }

    public CircleLocationTemplate setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public CircleLocationTemplate setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public CircleLocationTemplate setRadius(double radius) {
        this.radius = radius;
        return this;
    }
}
