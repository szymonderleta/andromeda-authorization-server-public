package pl.derleta.authorization.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PagedResourcesAssemblerArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
class WebMvcConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void getPage_withValidParameters_shouldInitializeHateoasBeans() {
        // Arrange
        HateoasPageableHandlerMethodArgumentResolver pageableResolver =
                applicationContext.getBean(HateoasPageableHandlerMethodArgumentResolver.class);
        HateoasSortHandlerMethodArgumentResolver sortResolver =
                applicationContext.getBean(HateoasSortHandlerMethodArgumentResolver.class);
        PagedResourcesAssembler<?> pagedResourcesAssembler =
                applicationContext.getBean(PagedResourcesAssembler.class);
        PagedResourcesAssemblerArgumentResolver pagedResourcesAssemblerArgumentResolver =
                applicationContext.getBean(PagedResourcesAssemblerArgumentResolver.class);

        // Act & Assert
        assertThat(pageableResolver).isNotNull();
        assertThat(sortResolver).isNotNull();
        assertThat(pagedResourcesAssembler).isNotNull();
        assertThat(pagedResourcesAssemblerArgumentResolver).isNotNull();
    }

    @Test
    void getPage_withAllowedCorsRequests_shouldAllowCorsRequestsFromAllowedOrigins() throws Exception {
        // Arrange
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Act & Assert
        mockMvc.perform(options("/some-endpoint")
                        .header("Origin", "https://localhost:3000")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "application/json")
                        .header("Access-Control-Request-Method", HttpMethod.POST.name()))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void rejectCorsRequests_withDisallowedHeader_shouldReturnForbidden() throws Exception {
        // Arrange
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Act
        mockMvc.perform(options("/some-endpoint")
                        .header("Origin", "https://localhost:3000")
                        .header("Disallowed-Header", "some-value"))
                // Assert
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void rejectCorsRequests_fromHttpLocalhost_shouldReturnForbidden() throws Exception {
        // Arrange
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Act
        mockMvc.perform(options("/some-endpoint")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", HttpMethod.GET.name()))
                // Assert
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectCorsRequests_fromDisallowedOrigins_shouldReturnForbidden() throws Exception {
        // Arrange
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Act
        mockMvc.perform(options("/some-endpoint")
                        .header("Origin", "https://malicious-site.com")
                        .header("Access-Control-Request-Method", HttpMethod.GET.name()))
                // Assert
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(status().isForbidden());
    }

}
