package pl.derleta.authorization.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pl.derleta.authorization.config.model.RoleSecurity;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;
import pl.derleta.authorization.controller.assembler.UserRoleModelAssembler;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRole;
import pl.derleta.authorization.service.UserRoleService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRoleControllerTest {

    @Autowired
    private UserRoleModelAssembler modelAssembler;

    @Autowired
    private PagedResourcesAssembler<UserRole> pagedResourcesAssembler;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRoleService service;

    UserRoleController controller;

    private final List<UserRole> testData = List.of(
            new UserRole(1, new User(1, "user1", "password1", "user1@example.com"), new Role(1, "ROLE_USER")),
            new UserRole(2, new User(2, "moderator", "password2", "moderator@example.com"), new Role(2, "ROLE_MODERATOR")),
            new UserRole(3, new User(3, "tester", "password3", "tester@example.com"), new Role(3, "ROLE_TESTER")),
            new UserRole(4, new User(4, "admin", "password4", "admin@example.com"), new Role(4, "ROLE_ADMIN"))
    );

    @BeforeEach
    void setUp() {
        controller = new UserRoleController(service, modelAssembler, pagedResourcesAssembler);

        when(service.getPage(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    int page = invocation.getArgument(0);
                    int size = invocation.getArgument(1);
                    String sortBy = invocation.getArgument(2);
                    String sortOrder = invocation.getArgument(3);
                    String usernameFilter = invocation.getArgument(4);
                    String emailFilter = invocation.getArgument(5);
                    String roleNameFilter = invocation.getArgument(6);


                    List<UserRole> filtered = testData.stream()
                            .filter(userRole -> (usernameFilter == null || userRole.user().username().contains(usernameFilter)))
                            .filter(userRole -> (emailFilter == null || userRole.user().email().contains(emailFilter)))
                            .filter(userRole -> (roleNameFilter == null || userRole.role().roleName().contains(roleNameFilter)))
                            .toList();

                    Comparator<UserRole> comparator = getUserRoleComparator(sortBy, sortOrder);

                    List<UserRole> sorted = filtered.stream().sorted(comparator).toList();

                    int start = page * size;
                    int end = Math.min(start + size, sorted.size());
                    List<UserRole> pagedTokens = sorted.subList(start, end);

                    return new PageImpl<>(pagedTokens, PageRequest.of(page, size), sorted.size());
                });

        when(service.get(anyLong())).thenAnswer(invocation ->
                testData.stream()
                        .filter(userRole -> userRole.userRoleId() == (long) invocation.getArgument(0))
                        .findFirst()
                        .orElse(null)
        );

        when(service.save(anyLong(), anyInt())).thenReturn(
                new UserRole(123, new User(123L, "New User", "password", "<PASSWORD>@<EMAIL>"), new Role(1, "ROLE_USER"))
        );

    }

    private static Comparator<UserRole> getUserRoleComparator(String sortBy, String sortOrder) {
        Comparator<UserRole> comparator;
        if ("email".equals(sortBy)) {
            comparator = Comparator.comparing(userRole -> userRole.user().email());
        } else if ("username".equals(sortBy)) {
            comparator = Comparator.comparing(userRole -> userRole.user().username());
        } else if ("roleName".equals(sortBy)) {
            comparator = Comparator.comparing(userRole -> userRole.role().roleName());
        } else {
            comparator = Comparator.comparing(UserRole::userRoleId);
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

    private ResultActions performGetPageRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/user-role";
        return performRequest(url, token, requestingAppHeader, params);
    }

    private ResultActions performGetRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/user-role/" + params.get("id");
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

    private ResultActions performAddRequest(String token, String requestingAppHeader, Long userId, Integer roleId) throws Exception {
        String url = "/api/v1/table/user-role/" + userId + "/" + roleId;

        MockHttpServletRequestBuilder request = post(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(request).andDo(print());
    }

    private ResultActions performDeleteRequest(String token, String requestingAppHeader, Long userId, Integer roleId) throws Exception {
        String url = "/api/v1/table/user-role/" + userId + "/" + roleId;

        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPage_withJwtTokenAndAdminRole_shouldReturnSuccess() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "username",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", "",
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetPageRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk());
    }

    @Test
    void getPage_withJwtTokenAndInvalidHeader_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "username",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", "",
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetPageRequest(token, "unknown_rest_api", params);

        // Assert
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    void getPage_withJwtTokenAndUserRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_NON_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "username",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", "",
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetPageRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    void getPage_withJwtTokenAndTesterRole_shouldReturnSuccess() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(3, "ROLE_TESTER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "5",
                "sortBy", "email",
                "sortOrder", "desc",
                "usernameFilter", "",
                "emailFilter", "",
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetPageRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk());
    }

    @Test
    void getPage_withNoJwtToken_shouldReturnUnauthorized() throws Exception {
        // Arrange
        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "email",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", "",
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetPageRequest(null, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    void getPage_withRoleNameFilter_shouldReturnFilteredResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "email",
                "sortOrder", "asc",
                "usernameFilter", "user1",
                "emailFilter", "",
                "roleNameFilter", ""
        );

        Page<UserRole> filteredPage = new PageImpl<>(List.of(testData.get(1)));
        when(service.getPage(0, 10, "email", "asc", "user1", "", ""))
                .thenReturn(filteredPage);

        // Act
        ResultActions resultActions = performGetPageRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk());
        verify(service).getPage(0, 10, "email", "asc", "user1", "", "");
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
                "id", "4"
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
                "id", "4"
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
                "id", "4"
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
                "id", "48"
        );

        // Act
        performGetRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isNotFound());
    }

    @Test
    void add_withJwtTokenAndAdminRole_shouldReturnSuccess() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 123L;
        int roleId = 1;

        // Act
        performAddRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isCreated());
    }

    @Test
    void add_withJwtTokenAndAdminRole_shouldReturnForbidden_forInvalidAppHeader() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 123L;
        int roleId = 1;

        // Act
        performAddRequest(token, "invalid_app_name", userId, roleId)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void add_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 123L;
        int roleId = 1;

        // Act
        performAddRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void add_withJwtTokenAndBadUserId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 48L;
        int roleId = 1;

        when(service.save(eq(48L), anyInt())).thenReturn(null);

        // Act
        performAddRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isNotFound());
    }

    @Test
    void add_withJwtTokenAndBadRoleId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 48L;
        int roleId = 48;

        when(service.save(anyLong(), eq(48))).thenReturn(null);

        // Act
        performAddRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_withJwtTokenAndAdminRole_shouldReturnOk() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        int roleId = 10;
        long userId = 10L;

        when(service.delete(userId, roleId)).thenReturn(true);

        // Act
        performDeleteRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isOk());
    }

    @Test
    void delete_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        int roleId = 10;
        long userId = 4L;

        // Act
        performDeleteRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_withJwtTokenAndInvalidAppHeader_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        int roleId = 10;
        long userId = 4L;

        // Act
        performDeleteRequest(token, "invalid_app_name", userId, roleId)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_withJwtTokenAndNonExistingRoleId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        int roleId = 10;
        long userId = 4L;

        when(service.delete(userId, roleId)).thenReturn(false);

        // Act
        performDeleteRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_withJwtTokenAndNonExistingUserId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        int roleId = 10;
        long userId = 48L;

        when(service.delete(userId, roleId)).thenReturn(false);

        // Act
        performDeleteRequest(token, "nebula_rest_api", userId, roleId)
                // Assert
                .andExpect(status().isNotFound());
    }

}
