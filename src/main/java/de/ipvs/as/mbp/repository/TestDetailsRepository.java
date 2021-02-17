package de.ipvs.as.mbp.repository;

import java.util.List;
import java.util.Optional;

import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.projection.ComponentExcerpt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Repository for test details of the tests that were created by the user with the testing-tool.
 */
@RepositoryRestResource(collectionResourceRel = "test-details", path = "test-details")
public interface TestDetailsRepository extends MongoRepository<TestDetails, String> {

    TestDetails findByName(@Param("name") String name);
    Optional<TestDetails> findById(@Param("id") String id);


}