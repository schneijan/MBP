package de.ipvs.as.mbp.domain.discovery.collections;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Objects of this class act as containers for one or multiple {@link CandidateDevicesCollection}s, each holding the
 * {@link DeviceDescription}s of the candidate devices that were retrieved from one single discovery repository for a
 * certain {@link DeviceTemplate}. While the pertaining {@link DeviceTemplate} is the same for all the
 * {@link CandidateDevicesCollection}s of the container, they represent the candidate devices of different discovery
 * repositories. As a result, overlaps between the {@link DeviceDescription}s of different
 * {@link CandidateDevicesCollection}s within the same {@link CandidateDevicesContainer} are possible, when
 * multiple discovery repositories contain device descriptions of the same devices.
 * Objects of this class provide methods that allow to retrieve, add, remove or replace
 * {@link CandidateDevicesCollection}s for certain discovery repositories, identified by their name.
 */
@Document(collection = "candidateDevices")
public class CandidateDevicesContainer {

    //The ID of the device template for which the candidate devices were retrieved
    @Id
    private String deviceTemplateId;

    //Map (repository name --> candidate devices) of candidate device collections
    private Map<String, CandidateDevicesCollection> candidateDevices;

    /**
     * Creates a new, empty candidate devices result container.
     */
    public CandidateDevicesContainer() {
        //Initialize data structures
        this.candidateDevices = new HashMap<>();
    }

    /**
     * Creates a new candidate devices result container from a given ID of a {@link DeviceTemplate} and multiple given
     * {@link CandidateDevicesCollection}s, one per repository from which the candidate devices could be retrieved.
     *
     * @param deviceTemplateId The ID of the {@link DeviceTemplate} for which the candidate devices were retrieved
     * @param candidateDevices The {@link CandidateDevicesCollection}s of the candidate devices to add
     */
    public CandidateDevicesContainer(String deviceTemplateId, Collection<CandidateDevicesCollection> candidateDevices) {
        //Set fields
        setDeviceTemplateId(deviceTemplateId);
        setCandidateDevices(candidateDevices);
    }

    /**
     * Returns the total number of {@link CandidateDevicesCollection}s from unique repositories that are available
     * within the result container.
     *
     * @return The total number of candidate device collections
     */
    public long getCollectionsCount() {
        //Stream through the candidate device collections and count them
        return this.candidateDevices.values().stream().distinct().count();
    }

    /**
     * Returns the total number of unique candidate devices that are available within the result container across
     * all {@link CandidateDevicesCollection}s.
     *
     * @return The total number of unique candidate devices
     */
    public long getCandidateDevicesCount() {
        //Stream through the candidate devices of the individual collections and count them
        return this.candidateDevices.values().stream()
                .distinct()
                .flatMap(CandidateDevicesCollection::stream)
                .distinct()
                .count();
    }

    /**
     * Returns whether the {@link CandidateDevicesContainer} contains any candidate devices.
     *
     * @return True, if candidate devices are available; false otherwise
     */
    public boolean isEmpty() {
        return getCandidateDevicesCount() > 0;
    }

    /**
     * Returns the descriptions of the candidate devices from a certain repository, given by its name,
     * as {@link CandidateDevicesCollection}.
     *
     * @param repositoryName The name of the repository
     * @return The candidate device descriptions for the repository
     */
    public CandidateDevicesCollection get(String repositoryName) {
        //Retrieve corresponding candidate devices from map
        return this.candidateDevices.get(repositoryName);
    }

    /**
     * Returns whether the {@link CandidateDevicesContainer} contains a {@link CandidateDevicesCollection} for a
     * certain discovery repository, given by its name.
     *
     * @param repositoryName The name of the discovery repository to check
     * @return True, if a {@link CandidateDevicesCollection} is available for this repository; false otherwise
     */
    public boolean contains(String repositoryName) {
        //Sanity check
        if ((repositoryName == null) || repositoryName.isEmpty()) return false;

        //Check whether there is a matching entry in the map
        return this.candidateDevices.containsKey(repositoryName);
    }

    /**
     * Sets all candidate devices that were retrieved for the various discovery repositories. Thereby, each
     * {@link CandidateDevicesCollection} represents the candidate devices that were received from one repository.
     *
     * @param candidateDevices The candidate devices to set
     */
    public void setCandidateDevices(Collection<CandidateDevicesCollection> candidateDevices) {
        //Stream through the collections of candidate devices and create a map from them
        this.candidateDevices = candidateDevices.stream()
                .collect(Collectors.toMap(CandidateDevicesCollection::getRepositoryName, c -> c));
    }

    /**
     * Adds the descriptions of new candidate devices for a certain discovery repository to the result container.
     *
     * @param candidateDevices The descriptions of the candidate devices to add
     */
    public void addCandidateDevices(CandidateDevicesCollection candidateDevices) {
        //Null check
        if (candidateDevices == null) {
            throw new IllegalArgumentException("The collection of candidate devices must not be null.");
        }

        //Add to map
        this.candidateDevices.put(candidateDevices.getRepositoryName(), candidateDevices);
    }

    /**
     * Adds the descriptions of new candidate devices for multiple discovery repositories to the result container.
     *
     * @param candidateDevices The description collections of the candidate devices to add
     */
    public void addCandidateDevices(Collection<CandidateDevicesCollection> candidateDevices) {
        //Null check
        if ((candidateDevices == null) || candidateDevices.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The candidate device collections must not be null.");
        }

        //Stream through the provided collections and add them to the map
        candidateDevices.forEach(this::addCandidateDevices);
    }

    /**
     * Removes the descriptions of the candidate devices of a certain repository, given by its name, from the
     * result container.
     *
     * @param repositoryName The name of the repository for which the descriptions of the candidate devices are
     *                       supposed to be removed
     */
    public void removeCandidateDevices(String repositoryName) {
        //Sanity check
        if ((repositoryName == null) || repositoryName.isEmpty()) {
            throw new IllegalArgumentException("The repository name must not be null or empty.");
        }

        //Remove from map
        candidateDevices.remove(repositoryName);
    }

    /**
     * Replaces the descriptions of the candidate devices of a certain repository, given by its name, with the
     * descriptions of new candidate devices. If no candidate device descriptions are currently present for the
     * given repository name, the new new candidate devices will be added nonetheless.
     *
     * @param repositoryName   The name of the repository for which the descriptions of its candidate devices are
     *                         supposed to be replaced
     * @param candidateDevices The descriptions of the new candidate devices
     */
    public void replaceCandidateDevices(String repositoryName, CandidateDevicesCollection candidateDevices) {
        //Null check
        if (candidateDevices == null) {
            throw new IllegalArgumentException("The collection of candidate devices must not be null.");
        }

        //Remove old candidate devices of the repository
        this.removeCandidateDevices(repositoryName);

        //Add new candidate devices
        this.addCandidateDevices(candidateDevices);
    }

    /**
     * Returns a human-readable string representation of the {@link CandidateDevicesContainer} by summarizing its key
     * properties.
     *
     * @return The human-readable description
     */
    public String toHumanReadableDescription() {
        //Create string representation
        return String.format(String.format("%d unique candidate devices from %d discovery repositories",
                this.getCandidateDevicesCount(), this.getCollectionsCount()));
    }

    /**
     * Returns the ID of the device template for which the candidate device descriptions as resulting from the various
     * discovery repositories are stored in this result container.
     *
     * @return The device template ID
     */
    public String getDeviceTemplateId() {
        return deviceTemplateId;
    }

    /**
     * Sets the ID of the device template for which the candidate device descriptions as resulting from the various
     * discovery repositories are stored in this result container.
     *
     * @param deviceTemplateId The device template ID to set
     * @return The result container
     */
    public CandidateDevicesContainer setDeviceTemplateId(String deviceTemplateId) {
        this.deviceTemplateId = deviceTemplateId;
        return this;
    }

    /**
     * Creates a stream from the {@link CandidateDevicesCollection}s that are part of the result container and
     * returns it.
     *
     * @return The resulting stream
     */
    public Stream<CandidateDevicesCollection> stream() {
        //Create the stream from the candidate devices collections
        return this.candidateDevices.values().stream();
    }
}
