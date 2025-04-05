package pl.derleta.authorization.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pl.derleta.authorization.config.model.RoleSecurity;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;
import pl.derleta.authorization.controller.assembler.UserRolesModelAssembler;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.service.UserRolesService;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRolesControllerTest {

    @Autowired
    private UserRolesModelAssembler modelAssembler;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRolesService service;

    UserRolesController controller;

    private final List<UserRoles> testData = List.of(
            new UserRoles(
                    new User(1, "user1", "password1", "user1@example.com"),
                    Set.of(
                            new Role(1, "ROLE_USER"),
                            new Role(2, "ROLE_MODERATOR")
                    )
            ),
            new UserRoles(
                    new User(2, "admin", "password2", "admin@example.com"),
                    Set.of(
                            new Role(3, "ROLE_ADMIN"),
                            new Role(4, "ROLE_TESTER")
                    )
            ),
            new UserRoles(
                    new User(3, "tester", "password3", "tester@example.com"),
                    Set.of(new Role(3, "ROLE_TESTER"))
            ),
            new UserRoles(
                    new User(4, "admin", "password4", "admin@example.com"),
                    Set.of(
                            new Role(4, "ROLE_ADMIN"),
                            new Role(5, "ROLE_USER")
                    )
            )
    );


    @BeforeEach
    void setUp() {
        controller = new UserRolesController(service, modelAssembler);

        when(service.get(anyLong(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    String sortBy = invocation.getArgument(1);
                    String sortOrder = invocation.getArgument(2);
                    String roleNameFilter = invocation.getArgument(3);

                    Comparator<Role> comparator = getRoleComparator(sortBy, sortOrder);

                    return testData.stream()
                            .filter(userRole -> userRole.user().userId() == userId)
                            .findFirst()
                            .map(userRole -> new UserRoles(
                                    userRole.user(),
                                    userRole.roles().stream()
                                            .filter(role -> roleNameFilter == null || role.roleName().contains(roleNameFilter))
                                            .sorted(comparator)
                                            .collect(Collectors.toCollection(LinkedHashSet::new))
                            ))
                            .orElse(null);
                });

    }

    private static Comparator<Role> getRoleComparator(String sortBy, String sortOrder) {
        Comparator<Role> comparator;
        if ("roleName".equals(sortBy)) {
            comparator = Comparator.comparing(Role::roleName);
        } else {
            comparator = Comparator.comparing(Role::roleId);
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private UserSecurity createUserWithRoles(Set<RoleSecurity> roles) {
        return new UserSecurity(
                1L,
                "Admin User",
                "admin@example.com",
                "password123",
                roles
        );
    }

    private String generateTokenForUser(UserSecurity user) {
        return jwtTokenUtil.generateAccessToken(user);
    }

    private ResultActions performGetRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/user-roles/" + params.get("id");
        return performRequest(url, token, requestingAppHeader, Map.of());
    }

    private ResultActions performRequest(String url, String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder request = get(url)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("X-Requesting-App", requestingAppHeader);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            request.param(entry.getKey(), entry.getValue());
        }

        return mockMvc.perform(request).andDo(print());
    }

    @Test
    void get_withJwtTokenAndAdminRole_shouldReturnSuccess() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "id", "4",
                "roleNameFilter", "ROLE_USER",
                "sortBy", "roleId",
                "sortOrder", "asc"
        );

        // Act
        performGetRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isOk());
    }

    @Test
    void get_withJwtTokenAndAdminRole_shouldReturnForbidden_forInvalidAppHeader() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "id", "4",
                "roleNameFilter", "ROLE_USER",
                "sortBy", "roleId",
                "sortOrder", "asc"
        );

        // Act
        performGetRequest(token, "invalid_app_name", params)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void get_withJwtTokenWithInvalidRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_MASTER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "id", "4",
                "roleNameFilter", "ROLE_USER",
                "sortBy", "roleId",
                "sortOrder", "asc"
        );

        // Act
        performGetRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void get_withJwtTokenAndBadId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "id", "43",
                "roleNameFilter", "ROLE_USER",
                "sortBy", "roleId",
                "sortOrder", "asc"
        );

        // Act
        performGetRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isNotFound());
    }

}
