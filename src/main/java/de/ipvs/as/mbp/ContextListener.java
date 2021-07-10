package de.ipvs.as.mbp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Provides context listeners that can be used to schedule tasks which need to be executed on startup or shutdown
 * of the application.
 */
@WebListener
public class ContextListener implements ServletContextListener {

    /**
     * This method is called on startup of the application.
     * @param servletContextEvent The corresponding servlet context event
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("Startup");
    }

    /**
     * This method is called on shutdown of the application.
     * @param servletContextEvent The corresponding servlet context event
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Shutdown");
    }
}
