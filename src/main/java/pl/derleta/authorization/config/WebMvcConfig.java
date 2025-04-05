package pl.derleta.authorization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PagedResourcesAssemblerArgumentResolver;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for Spring MVC and HATEOAS-related settings.
 * <p>
 * This class implements the {@link WebMvcConfigurer} interface to configure essential
 * web settings such as CORS support. It also enables support for Hypermedia APIs in HAL format
 * via the {@link EnableHypermediaSupport} annotation.
 * <p>
 * The configuration provides beans for pageable and sort resolvers, which are commonly
 * used in constructing pageable REST APIs. Additionally, it creates a bean for
 * {@link PagedResourcesAssembler}, which aids in assembling paginated resources.
 * <p>
 * Features included in this class:
 * - Configures CORS (Cross-Origin Resource Sharing) mappings to allow cross-domain requests.
 * - Registers resolvers and assemblers for HATEOAS-based pageable and sortable APIs.
 * <p>
 * Beans:
 * - HateoasPageableHandlerMethodArgumentResolver: Resolves pageable parameters in HATEOAS.
 * - HateoasSortHandlerMethodArgumentResolver: Resolves sort parameters in HATEOAS.
 * - PagedResourcesAssembler: Assembles paged resources.
 * - PagedResourcesAssemblerArgumentResolver: Argument resolver for {@link PagedResourcesAssembler}.
 */
@Configuration
@ComponentScan
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Configures Cross-Origin Resource Sharing (CORS) mappings for the application.
     * This method defines the allowed origins, methods, headers, and other CORS-related settings
     * for incoming HTTP requests to enable secure resource sharing across different domains.
     *
     * @param registry the {@code CorsRegistry} instance that provides methods to define CORS configurations
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://localhost:3000", "https://milkyway.local")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
//                .allowedHeaders("Content-Type", "Authorization", "X-Requesting-App", "Accept", "Origin")
                .allowCredentials(true);
    }

    /**
     * Creates and returns a {@link HateoasPageableHandlerMethodArgumentResolver} bean.
     * <p>
     * This method initializes a {@link HateoasPageableHandlerMethodArgumentResolver} instance
     * configured with a {@link HateoasSortHandlerMethodArgumentResolver}. It is primarily used
     * to handle pageable parameters in HATEOAS-based APIs, enabling efficient resolution and
     * processing of pageable requests.
     *
     * @return a {@link HateoasPageableHandlerMethodArgumentResolver} instance configured
     * for handling pageable parameters in HATEOAS-based APIs.
     */
    @Bean
    public HateoasPageableHandlerMethodArgumentResolver pageableResolver() {
        return new HateoasPageableHandlerMethodArgumentResolver(sortResolver());
    }

    /**
     * Creates and returns a {@link HateoasSortHandlerMethodArgumentResolver} bean.
     * <p>
     * This method provides a {@link HateoasSortHandlerMethodArgumentResolver} instance,
     * enabling the resolution of sort parameters in HATEOAS-based APIs. It is commonly
     * used in conjunction with pageable handlers or other components that require sort handling.
     *
     * @return a {@link HateoasSortHandlerMethodArgumentResolver} instance for handling sort parameters.
     */
    @Bean
    public HateoasSortHandlerMethodArgumentResolver sortResolver() {
        return new HateoasSortHandlerMethodArgumentResolver();
    }

    /**
     * Creates and returns a {@link PagedResourcesAssembler} bean.
     * <p>
     * This method provides a {@link PagedResourcesAssembler} instance configured with
     * a {@link HateoasPageableHandlerMethodArgumentResolver}. The assembler is used to convert
     * pageable query results into hypermedia-based paginated representations, enabling the
     * creation of pageable HATEOAS-compliant APIs.
     *
     * @return a {@link PagedResourcesAssembler} instance suitable for assembling
     * paged resources in a HATEOAS-based API.
     */
    @Bean
    public PagedResourcesAssembler<?> pagedResourcesAssembler() {
        return new PagedResourcesAssembler<>(pageableResolver(), null);
    }

    /**
     * Creates and returns a {@link PagedResourcesAssemblerArgumentResolver} bean.
     * <p>
     * This method initializes an argument resolver that facilitates the construction
     * of {@link PagedResourcesAssembler} instances. It resolves pageable parameters
     * and binds them to the PagedResourcesAssembler for use in creating paginated responses.
     *
     * @return a {@link PagedResourcesAssemblerArgumentResolver} instance configured with a
     * {@link HateoasPageableHandlerMethodArgumentResolver}.
     */
    @Bean
    public PagedResourcesAssemblerArgumentResolver pagedResourcesAssemblerArgumentResolver() {
        return new PagedResourcesAssemblerArgumentResolver(pageableResolver());
    }

}
