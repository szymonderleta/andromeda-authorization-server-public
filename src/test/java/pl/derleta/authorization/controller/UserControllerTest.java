package pl.derleta.authorization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.derleta.authorization.controller.assembler.UserModelAssembler;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.service.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private UserModelAssembler modelAssembler;

    @Autowired
    private PagedResourcesAssembler<User> pagedResourcesAssembler;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    UserController controller;

    private final List<User> testData = List.of(
            new User(1, "user1", "password1", "user1@example.com"),
            new User(2, "moderator", "password2", "moderator@example.com"),
            new User(3, "tester", "password3", "tester@example.com"),
            new User(4, "admin", "password4", "admin@example.com")
    );

    @BeforeEach
    void setUp() {
        controller = new UserController(service, modelAssembler, pagedResourcesAssembler);

        when(service.getPage(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    int page = invocation.getArgument(0);
                    int size = invocation.getArgument(1);
                    String sortBy = invocation.getArgument(2);
                    String sortOrder = invocation.getArgument(3);
                    String usernameFilter = invocation.getArgument(4);
                    String emailFilter = invocation.getArgument(5);


                    List<User> filtered = testData.stream()
                            .filter(user -> (usernameFilter == null || user.username().contains(usernameFilter)))
                            .filter(user -> (emailFilter == null || user.email().contains(emailFilter)))
                            .toList();

                    Comparator<User> comparator = getUserComparator(sortBy, sortOrder);

                    List<User> sorted = filtered.stream().sorted(comparator).toList();

                    int start = page * size;
                    int end = Math.min(start + size, sorted.size());
                    List<User> pagedTokens = sorted.subList(start, end);

                    return new PageImpl<>(pagedTokens, PageRequest.of(page, size), sorted.size());
                });

        when(service.get(anyLong())).thenAnswer(invocation ->
                testData.stream()
                        .filter(user -> user.userId() == (long) invocation.getArgument(0))
                        .findFirst()
                        .orElse(null)
        );

        when(service.save(any())).thenReturn(
                new User(123, "New User", "password", "<PASSWORD>@<EMAIL>")
        );

        when(service.update(anyInt(), any(User.class))).thenReturn(
                new User(998877, "New User", "password", "<PASSWORD>@<EMAIL>")
        );

    }

    private static Comparator<User> getUserComparator(String sortBy, String sortOrder) {
        Comparator<User> comparator;
        if ("email".equals(sortBy)) {
            comparator = Comparator.comparing(User::email);
        } else if ("username".equals(sortBy)) {
            comparator = Comparator.comparing(User::username);
        } else {
            comparator = Comparator.comparing(User::userId);
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
        String url = "/api/v1/table/users";
        return performRequest(url, token, requestingAppHeader, params);
    }

    private ResultActions performGetRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/users/" + params.get("id");
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

    private ResultActions performAddRequest(String token, String requestingAppHeader, User user) throws Exception {
        String url = "/api/v1/table/users";
        String body = """
                {
                    "userId": "%s",
                    "username": "%s",
                    "password": "%s",
                    "email": "%s"
                }
                """.formatted(user.userId(), user.username(), user.password(), user.email());

        MockHttpServletRequestBuilder request = post(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(request).andDo(print());
    }

    private ResultActions performUpdateRequest(String token, String requestingAppHeader, Long userId, User user) throws Exception {
        String url = "/api/v1/table/users/" + userId;

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(user);

        MockHttpServletRequestBuilder request = put(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(request).andDo(print());
    }

    private ResultActions performDeleteRequest(String token, String requestingAppHeader, Long userId) throws Exception {
        String url = "/api/v1/table/users/" + userId;

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
                "emailFilter", ""
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
                "emailFilter", ""
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
                "emailFilter", ""
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
                "emailFilter", ""
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
                "emailFilter", ""
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
                "emailFilter", ""
        );

        Page<User> filteredPage = new PageImpl<>(List.of(testData.get(1)));
        when(service.getPage(0, 10, "email", "asc", "user1", ""))
                .thenReturn(filteredPage);

        // Act
        ResultActions resultActions = performGetPageRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk());
        verify(service).getPage(0, 10, "email", "asc", "user1", "");
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

        User userToCreate = new User(123L, "New User", "password", "<PASSWORD>");

        // Act & Assert
        performAddRequest(token, "nebula_rest_api", userToCreate)
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

        User userToCreate = new User(123L, "New User", "password", "<PASSWORD>");

        // Act & Assert
        performAddRequest(token, "invalid_app_name", userToCreate)
                .andExpect(status().isForbidden());
    }

    @Test
    void add_withJwtTokenAndWithUnknownRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_NOT_USER")
        ));

        String token = generateTokenForUser(user);

        User userToCreate = new User(123L, "New User", "password", "<PASSWORD>");

        // Act & Assert
        performAddRequest(token, "nebula_rest_api", userToCreate)
                .andExpect(status().isForbidden());
    }

    @Test
    void update_withJwtTokenAndAdminRole_shouldReturnSuccess() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 4;
        User userToUpdate = new User(4, "testuser", "password123", "testuser@example.com");

        when(service.update(eq(4L), any(User.class)))
                .thenReturn(new User(4L, "testuser", "password123", "testuser@example.com"));

        // Act
        performUpdateRequest(token, "nebula_rest_api", userId, userToUpdate)
                // Assert
                .andExpect(status().isOk()) // Oczekiwany wynik 200
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }

    @Test
    void update_withJwtTokenAndAdminRole_shouldReturnForbidden_forInvalidAppHeader() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 123;
        User userToUpdate = new User(123, "New User", "password", "<PASSWORD>@<EMAIL>");

        // Act & Assert
        performUpdateRequest(token, "invalid_app_name", userId, userToUpdate)
                .andExpect(status().isForbidden());
    }

    @Test
    void update_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 123;
        User userToUpdate = new User(123, "New User", "password", "<PASSWORD>@<EMAIL>");

        // Act & Assert
        performUpdateRequest(token, "nebula_rest_api", userId, userToUpdate)
                .andExpect(status().isForbidden());
    }

    @Test
    void update_withJwtTokenAndBadRoleId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 123;
        User userToUpdate = new User(123, "New User", "password", "<PASSWORD>@<EMAIL>");

        when(service.update(eq(124), any())).thenReturn(null);

        // Act & Assert
        performUpdateRequest(token, "nebula_rest_api", userId, userToUpdate)
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

        long userId = 10;

        when(service.delete(userId)).thenReturn(true);

        // Act
        performDeleteRequest(token, "nebula_rest_api", userId)
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

        long userId = 10;

        // Act
        performDeleteRequest(token, "nebula_rest_api", userId)
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

        long userId = 10;

        // Act
        performDeleteRequest(token, "invalid_app_name", userId)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_withJwtTokenAndNonExistingUserId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long userId = 10;

        when(service.delete(userId)).thenReturn(false);

        // Act
        performDeleteRequest(token, "nebula_rest_api", userId)
                // Assert
                .andExpect(status().isNotFound());
    }

}
