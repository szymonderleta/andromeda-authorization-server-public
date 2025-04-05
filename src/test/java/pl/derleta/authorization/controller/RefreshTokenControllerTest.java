package pl.derleta.authorization.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import pl.derleta.authorization.controller.assembler.RefreshTokenModelAssembler;
import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.service.token.RefreshTokenService;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RefreshTokenControllerTest {

    @Autowired
    private RefreshTokenModelAssembler tokenModelAssembler;

    @Autowired
    private PagedResourcesAssembler<RefreshToken> pagedResourcesAssembler;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService service;

    RefreshTokenController controller;

    private final List<RefreshToken> testData = List.of(
            new RefreshToken(1L, "token1", new User(1L, "specificUser", "password", "user1@example.com"), Timestamp.valueOf("2026-12-31 23:59:59")),
            new RefreshToken(2L, "token2", new User(2L, "testUser", "password", "user2@example.com"), Timestamp.valueOf("2026-12-30 23:59:59")),
            new RefreshToken(3L, "token3", new User(3L, "specificUser", "password", "user3@example.com"), Timestamp.valueOf("2026-12-29 23:59:59")),
            new RefreshToken(4L, "token4", new User(4L, "unknownUser", "password", "user4@domain.com"), Timestamp.valueOf("2026-12-28 23:59:59")),
            new RefreshToken(5L, "token5", new User(5L, "specificUser", "password", "user5@example.com"), Timestamp.valueOf("2026-12-27 23:59:59")),
            new RefreshToken(6L, "token6", new User(6L, "johnDoe", "password", "user6@example.com"), Timestamp.valueOf("2026-12-26 23:59:59")),
            new RefreshToken(7L, "token7", new User(7L, "specificUser", "password", "user7@example.com"), Timestamp.valueOf("2026-12-25 23:59:59")),
            new RefreshToken(8L, "token8", new User(8L, "specificUser", "password", "user8@example.com"), Timestamp.valueOf("2026-12-24 23:59:59")),
            new RefreshToken(9L, "token9", new User(9L, "specificUser", "password", "user9@example.com"), Timestamp.valueOf("2026-12-23 23:59:59")),
            new RefreshToken(10L, "token10", new User(10L, "specificUser", "password", "user10@example.com"), Timestamp.valueOf("2026-12-22 23:59:59")),
            new RefreshToken(11L, "token11", new User(11L, "testUser", "password", "user11@example.com"), Timestamp.valueOf("2026-12-21 23:59:59"))
    );

    private UserSecurity createUserWithRoles(Set<RoleSecurity> roles) {
        return new UserSecurity(
                1L,
                "Admin User",
                "admin@example.com",
                "password123",
                roles
        );
    }

    @BeforeEach
    void setUp() {
        controller = new RefreshTokenController(service, tokenModelAssembler, pagedResourcesAssembler);

        when(service.getPage(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    int page = invocation.getArgument(0);
                    int size = invocation.getArgument(1);
                    String sortBy = invocation.getArgument(2);
                    String sortOrder = invocation.getArgument(3);
                    String usernameFilter = invocation.getArgument(4);
                    String emailFilter = invocation.getArgument(5);

                    List<RefreshToken> filtered = testData.stream()
                            .filter(token -> (usernameFilter == null || token.user().username().contains(usernameFilter)))
                            .filter(token -> (emailFilter == null || token.user().email().contains(emailFilter)))
                            .toList();

                    Comparator<RefreshToken> comparator = getRefreshTokenComparator(sortBy, sortOrder);

                    List<RefreshToken> sorted = filtered.stream().sorted(comparator).toList();

                    int start = page * size;
                    int end = Math.min(start + size, sorted.size());
                    List<RefreshToken> pagedTokens = sorted.subList(start, end);

                    return new PageImpl<>(pagedTokens, PageRequest.of(page, size), sorted.size());
                });

        when(service.getValid(anyInt(), anyInt(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    int page = invocation.getArgument(0);
                    int size = invocation.getArgument(1);
                    String sortBy = invocation.getArgument(2);
                    String sortOrder = invocation.getArgument(3);

                    Comparator<RefreshToken> comparator = getRefreshTokenComparator(sortBy, sortOrder);

                    List<RefreshToken> sorted = testData.stream().sorted(comparator).toList();

                    int start = page * size;
                    int end = Math.min(start + size, sorted.size());
                    List<RefreshToken> pagedTokens = sorted.subList(start, end);

                    return new PageImpl<>(pagedTokens, PageRequest.of(page, size), sorted.size());
                });

        when(service.get(anyLong())).thenAnswer(invocation ->
                testData.stream()
                        .filter(token -> token.tokenId() == (long) invocation.getArgument(0))
                        .findFirst()
                        .orElse(null)
        );

        when(service.save(anyLong(), anyString())).thenReturn(
                new RefreshToken(1L, "token content", new User(4L, "existingUser", "password", "user4@example.com"), Timestamp.valueOf("2026-12-31 23:59:59"))
        );

    }

    private static Comparator<RefreshToken> getRefreshTokenComparator(String sortBy, String sortOrder) {
        Comparator<RefreshToken> comparator;
        if ("email".equals(sortBy)) {
            comparator = Comparator.comparing(token -> token.user().email());
        } else if ("username".equals(sortBy)) {
            comparator = Comparator.comparing(token -> token.user().username());
        } else {
            comparator = Comparator.comparing(RefreshToken::tokenId);
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private String generateTokenForUser(UserSecurity user) {
        return jwtTokenUtil.generateAccessToken(user);
    }

    private ResultActions performGetPageRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/tokens/refresh";
        return performRequest(url, token, requestingAppHeader, params);
    }

    private ResultActions performGetValidRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/tokens/refresh/valid";
        return performRequest(url, token, requestingAppHeader, params);
    }

    private ResultActions performGetRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/tokens/refresh/" + params.get("id");
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

    private ResultActions performAddRequest(String token, String requestingAppHeader, Long userId, String tokenToAdd) throws Exception {
        String url = "/api/v1/table/tokens/refresh/" + userId;
        String body = """
                {
                    "token": "%s"
                }
                """.formatted(tokenToAdd);

        MockHttpServletRequestBuilder request = post(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(request).andDo(print());
    }

    private ResultActions performDeleteRequest(String token, String requestingAppHeader, Long tokenId, Long userId) throws Exception {
        String url = "/api/v1/table/tokens/refresh/" + tokenId + "/" + userId;

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
                new RoleSecurity(2, "ROLE_USER"),
                new RoleSecurity(3, "ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", ""
        );

        // Act
        performGetPageRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isOk());
    }

    @Test
    void getPage_withJwtTokenAndAdminRole_shouldReturnForbidden_forInvalidAppHeader() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", ""
        );

        // Act
        performGetPageRequest(token, "invalid_app_name", params)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void getPage_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", ""
        );

        // Act
        performGetPageRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void getPage_withValidSortByAndSortOrder_shouldReturnSortedResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "username",
                "sortOrder", "desc",
                "usernameFilter", "",
                "emailFilter", ""
        );

        // Act
        performGetPageRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList[0].user.username").isNotEmpty())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList").isArray());
    }

    @Test
    void getPage_withUsernameFilter_shouldReturnFilteredResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc",
                "usernameFilter", "unknownUser",
                "emailFilter", ""
        );

        // Act
        performGetPageRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList").isArray())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList[0].user.username").value("unknownUser"));
    }

    @Test
    void getPage_withEmailFilter_shouldReturnFilteredResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", "user8@example.com"
        );

        // Act
        performGetPageRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList").isArray())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList[0].user.email").value("user8@example.com"));
    }

    @Test
    void getPage_withPageAndSize_shouldReturnPaginatedResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "1",
                "size", "5",
                "sortBy", "userId",
                "sortOrder", "asc",
                "usernameFilter", "",
                "emailFilter", ""
        );

        // Act
        performGetPageRequest(token, "nebula_rest_api", params)
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList").isArray())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList.length()").value(5));
    }

    @Test
    void getPage_withFiltersAndSorting_shouldReturnFilteredAndSortedResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "email",
                "sortOrder", "desc",
                "usernameFilter", "specificUser",
                "emailFilter", "@example.com"
        );

        // Act & Assert
        performGetPageRequest(token, "nebula_rest_api", params)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList").isArray())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList", hasSize(7))) // oczekujemy 7 elementów
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList[0].tokenId").value(9));
    }


    @Test
    void getValid_withJwtTokenAndAdminRole_shouldReturnSuccess() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc"
        );

        // Act & Assert
        performGetValidRequest(token, "nebula_rest_api", params)
                .andExpect(status().isOk());
    }

    @Test
    void getValid_withJwtTokenAndAdminRole_shouldReturnForbidden_forInvalidAppHeader() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc"
        );

        // Act & Assert
        performGetValidRequest(token, "invalid_app_name", params)
                .andExpect(status().isForbidden());
    }

    @Test
    void getValid_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "userId",
                "sortOrder", "asc"
        );

        // Act & Assert
        performGetValidRequest(token, "nebula_rest_api", params)
                .andExpect(status().isForbidden());
    }

    @Test
    void getValid_withValidSortByAndSortOrder_shouldReturnSortedResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "0",
                "size", "10",
                "sortBy", "username",
                "sortOrder", "desc"
        );

        // Act & Assert
        performGetValidRequest(token, "nebula_rest_api", params)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList[0].user.username").isNotEmpty())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList").isArray());
    }

    @Test
    void getValid_withPageAndSize_shouldReturnPaginatedResults() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "page", "1",
                "size", "5",
                "sortBy", "userId",
                "sortOrder", "asc"
        );

        // Act & Assert
        performGetValidRequest(token, "nebula_rest_api", params)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList").isArray())
                .andExpect(jsonPath("$._embedded.refreshTokenResponseList.length()").value(5));
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

        // Act & Assert
        performGetRequest(token, "nebula_rest_api", params)
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

        // Act & Assert
        performGetRequest(token, "invalid_app_name", params)
                .andExpect(status().isForbidden());
    }

    @Test
    void get_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "id", "4"
        );

        // Act & Assert
        performGetRequest(token, "nebula_rest_api", params)
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

        // Act & Assert
        performGetRequest(token, "nebula_rest_api", params)
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

        Long userId = 4L;
        String tokenToAdd = "token body";

        // Act & Assert
        performAddRequest(token, "nebula_rest_api", userId, tokenToAdd)
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

        Long userId = 4L;
        String tokenToAdd = "newToken";

        // Act & Assert
        performAddRequest(token, "invalid_app_name", userId, tokenToAdd)
                .andExpect(status().isForbidden());
    }

    @Test
    void add_withJwtTokenAndWithoutAdminOrModeratorRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Long userId = 4L;
        String tokenToAdd = "newToken";

        // Act & Assert
        performAddRequest(token, "nebula_rest_api", userId, tokenToAdd)
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

        Long userId = 48L;
        String tokenToAdd = "newToken";

        when(service.save(eq(48L), anyString())).thenReturn(null);

        // Act & Assert
        performAddRequest(token, "nebula_rest_api", userId, tokenToAdd)
                .andExpect(status().isNotFound());
    }

    @Test
    void add_withJwtTokenAndEmptyToken_shouldReturnBadRequest() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Long userId = 4L;
        String tokenToAdd = "";

        // Act & Assert
        performAddRequest(token, "nebula_rest_api", userId, tokenToAdd)
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_withJwtTokenAndAdminRole_shouldReturnOk() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long tokenId = 10L;
        long userId = 10L;

        when(service.delete(tokenId, userId)).thenReturn(true);

        // Act & Assert
        performDeleteRequest(token, "nebula_rest_api", tokenId, userId)
                .andExpect(status().isOk());
    }

    @Test
    void delete_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long tokenId = 10L;
        long userId = 4L;

        // Act & Assert
        performDeleteRequest(token, "nebula_rest_api", tokenId, userId)
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

        long tokenId = 10L;
        long userId = 4L;

        // Act & Assert
        performDeleteRequest(token, "invalid_app_name", tokenId, userId)
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_withJwtTokenAndNonExistingTokenId_shouldReturnNotFound() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        long tokenId = 999L;
        long userId = 4L;

        when(service.delete(tokenId, userId)).thenReturn(false);

        // Act & Assert
        performDeleteRequest(token, "nebula_rest_api", tokenId, userId)
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

        long tokenId = 10L;
        long userId = 48L;

        when(service.delete(tokenId, userId)).thenReturn(false);

        // Act & Assert
        performDeleteRequest(token, "nebula_rest_api", tokenId, userId)
                .andExpect(status().isNotFound());
    }

}
