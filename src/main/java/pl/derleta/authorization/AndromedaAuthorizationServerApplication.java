package pl.derleta.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the Andromeda Authorization Server application.
 * This class is responsible for bootstrapping the Spring Boot application.
 * <p>
 * It contains the main method which launches the application using {@link SpringApplication#run(Class, String[])}.
 * <p>
 * Constants defined in this class, such as DEFAULT_PAGE_SIZE, can be utilized throughout the application to ensure consistency,
 * such as default configuration values.
 */
@SpringBootApplication
public class AndromedaAuthorizationServerApplication {

    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * The main method serves as the entry point for the Andromeda Authorization Server application.
     * It initializes and launches the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(AndromedaAuthorizationServerApplication.class, args);
    }

}
