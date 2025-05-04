package pl.derleta.authorization.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import pl.derleta.authorization.AndromedaAuthorizationServerApplication;

/**
 * ServletInitializer is a specialized implementation of {@link SpringBootServletInitializer}
 * that supports configuring the application when it is deployed as a traditional WAR package.
 * <p>
 * This class overrides the {@code configure} method to specify the primary source of the Spring
 * Boot application, which in this case is {@code AndromedaAuthorizationServerApplication}.
 * <p>
 * By extending {@code SpringBootServletInitializer}, this class enables the application to run
 * in servlet containers such as Tomcat, Jetty, or WildFly when deployed as a WAR.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /**
     * Configures the application when deployed as a traditional WAR package.
     * <p>
     * This method overrides the {@code configure} method of {@code SpringBootServletInitializer}
     * to specify the primary source of the Spring Boot application.
     *
     * @param application the {@code SpringApplicationBuilder} used to configure the application
     *                    context for the servlet environment
     * @return the {@code SpringApplicationBuilder} with sources set to {@code AndromedaAuthorizationServerApplication}
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AndromedaAuthorizationServerApplication.class);
    }

}
