package de.ipvs.as.mbp.repository;

import java.util.Optional;

import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.repository.projection.MonitoringOperatorExcerpt;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Repository definition interface for monitoring adapters.
 */
//@RepositoryRestResource(collectionResourceRel = "monitoring-adapters", path = "monitoring-adapters",
//        excerptProjection = MonitoringAdapterExcerpt.class)
//@Api(tags = {"Monitoring adapter entities"}, description = "CRUD for monitoring adapter entities")
public interface MonitoringOperatorRepository extends UserEntityRepository<MonitoringOperator> {
//    @RestResource(exported = false)
//    MonitoringAdapter findByName(@Param("name") String name);

    @RestResource(exported = false)
    Optional<MonitoringOperatorExcerpt> findExcerptById(@Param("id") String id);

//    @Override
//    @PreAuthorize("@repositorySecurityGuard.checkPermission(#adapter, 'delete')")
//    @ApiOperation(value = "Deletes a monitoring adapter entity", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the monitoring adapter entity"), @ApiResponse(code = 404, message = "Monitoring adapter entity not found")})
//    void delete(@Param("adapter") @ApiParam(value = "The ID of the monitoring adapter entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) MonitoringAdapter adapter);
//
//    @Override
//    @Query("{_id: null}") //Fail fast
//    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @monitoringAdapterRepository)")
//    @ApiOperation(value = "Retrieves all monitoring adapter entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    Page<MonitoringAdapter> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}