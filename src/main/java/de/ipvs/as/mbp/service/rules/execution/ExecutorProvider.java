package de.ipvs.as.mbp.service.rules.execution;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Allows to look up Spring managed rule action executor beans by class and to provide them to non-bean classes.
 */
@Component
public class ExecutorProvider implements ApplicationContextAware {
    private static ApplicationContext context;

    /**
     * Returns the Spring managed rule action executor bean of a given rule action executor class type.
     * Returns null otherwise.
     *
     * @param executorClass The class of the rule action executor
     * @return The corresponding rule action executor bean
     */
    public static <T extends RuleActionExecutor> T get(Class<T> executorClass) {
        return context.getBean(executorClass);
    }

    /**
     * Sets the internal application context.
     *
     * @param applicationContext The application context to set
     * @throws BeansException In case of an unexpected error
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
