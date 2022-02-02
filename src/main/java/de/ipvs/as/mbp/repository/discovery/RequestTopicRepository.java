package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.UserEntityRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for storing and managing {@link RequestTopic}s.
 */
@Repository
public interface RequestTopicRepository extends UserEntityRepository<RequestTopic> {

}
