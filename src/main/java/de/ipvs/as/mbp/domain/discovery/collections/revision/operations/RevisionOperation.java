package de.ipvs.as.mbp.domain.discovery.collections.revision.operations;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

/**
 * Generic interface for operations that are supposed to be executed on a {@link CandidateDevicesCollection} that
 * was received from a discovery repository on behalf of a certain {@link DeviceTemplate}.
 */
public interface RevisionOperation {
    /**
     * Returns the type name of the operation.
     *
     * @return The type name
     */
    String getTypeName();

    /**
     * Applies the operation to a given {@link CandidateDevicesCollection} that was received from a discovery repository
     * on behalf of a certain {@link DeviceTemplate}.
     *
     * @param collection The {@link CandidateDevicesCollection} to which the operation is supposed to be applied
     */
    void apply(CandidateDevicesCollection collection);

    /**
     * Returns a human-readable string representation of the {@link RevisionOperation}.
     *
     * @return The human-readable description
     */
    String toHumanReadableDescription();
}
