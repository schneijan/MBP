package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "list", types = Operator.class)
public interface OperatorExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    String getDescription();

    String getUnit();

    List<Parameter> getParameters();
}
