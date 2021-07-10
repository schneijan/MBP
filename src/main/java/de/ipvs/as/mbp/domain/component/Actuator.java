package de.ipvs.as.mbp.domain.component;

import de.ipvs.as.mbp.domain.user_entity.MBPEntity;

@MBPEntity(createValidator = ComponentCreateValidator.class, deleteValidator = ComponentDeleteValidator.class,
        createEventHandler = ComponentCreateEventHandler.class)
public class Actuator extends Component {

    private static final String COMPONENT_TYPE_NAME = "actuator";

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }
}
