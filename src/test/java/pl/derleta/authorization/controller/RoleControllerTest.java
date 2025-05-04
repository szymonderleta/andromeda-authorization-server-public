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
import pl.derleta.authorization.controller.assembler.RoleModelAssembler;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.service.RoleService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest {

    @Autowired
    private RoleModelAssembler roleModelAssembler;

    @Autowired
    private PagedResourcesAssembler<Role> pagedResourcesAssembler;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService service;

    RoleController controller;


    private final List<Role> testData = List.of(
            new Role(1, "ROLE_USER"),
            new Role(2, "ROLE_MODERATOR"),
            new Role(3, "ROLE_TESTER"),
            new Role(4, "ROLE_ADMIN")
    );

    @BeforeEach
    void setUp() {
        controller = new RoleController(service, roleModelAssembler, pagedResourcesAssembler);

        when(service.getList(anyString()))
                .thenAnswer(invocation -> {
                    String roleNameFilter = invocation.getArgument(0);

                    return testData.stream()
                            .filter(role -> (roleNameFilter == null || role.roleName().contains(roleNameFilter)))
                            .collect(Collectors.toSet());
                });

        when(service.getPage(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    int page = invocation.getArgument(0);
                    int size = invocation.getArgument(1);
                    String sortBy = invocation.getArgument(2);
                    String sortOrder = invocation.getArgument(3);
                    String roleNameFilter = invocation.getArgument(4);

                    List<Role> filtered = testData.stream()
                            .filter(role -> (roleNameFilter == null || role.roleName().contains(roleNameFilter)))
                            .toList();

                    Comparator<Role> comparator = getRoleComparator(sortBy, sortOrder);

                    List<Role> sorted = filtered.stream().sorted(comparator).toList();

                    int start = page * size;
                    int end = Math.min(start + size, sorted.size());
                    List<Role> pagedTokens = sorted.subList(start, end);

                    return new PageImpl<>(pagedTokens, PageRequest.of(page, size), sorted.size());
                });

        when(service.get(anyInt())).thenAnswer(invocation ->
                testData.stream()
                        .filter(role -> role.roleId() == (int) invocation.getArgument(0))
                        .findFirst()
                        .orElse(null)
        );

        when(service.save(any())).thenReturn(
                new Role(77, "ROLE_UPDATER")
        );

        when(service.update(anyInt(), any())).thenReturn(
                new Role(77, "ROLE_UPDATER")
        );

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

    private ResultActions performGetListRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/roles/list";
        return performRequest(url, token, requestingAppHeader, params);
    }

    private ResultActions performGetPageRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/roles";
        return performRequest(url, token, requestingAppHeader, params);
    }

    private ResultActions performGetRequest(String token, String requestingAppHeader, Map<String, String> params) throws Exception {
        String url = "/api/v1/table/roles/" + params.get("id");
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

    private ResultActions performAddRequest(String token, String requestingAppHeader, Role role) throws Exception {
        String url = "/api/v1/table/roles";
        String body = """
                {
                    "roleId": "%s",
                    "roleName": "%s"
                }
                """.formatted(role.roleId(), role.roleName());

        MockHttpServletRequestBuilder request = post(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(request).andDo(print());
    }

    private ResultActions performDeleteRequest(String token, String requestingAppHeader, Integer roleId) throws Exception {
        String url = "/api/v1/table/roles/" + roleId;

        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions performUpdateRequest(String token, String requestingAppHeader, Integer roleId, Role role) throws Exception {
        String url = "/api/v1/table/roles/" + roleId;
        String body = """
                {
                    "roleId": "%s",
                    "roleName": "%s"
                }
                """.formatted(role.roleId(), role.roleName());

        MockHttpServletRequestBuilder request = put(url)
                .header("Authorization", "Bearer " + token)
                .header("X-Requesting-App", requestingAppHeader)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(request).andDo(print());
    }


    @Test
    void getList_withAdminRole_shouldReturnCollection() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetListRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk());
    }

    @Test
    void getList_withRoleTester_shouldReturnCollection() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(3, "ROLE_TESTER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetListRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk());
    }

    @Test
    void getList_withUnknownHeader_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(3, "ROLE_TESTER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetListRequest(token, "nebula-rest_api", params);

        // Assert
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    void getList_withInvalidRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(3, "ROLE_TER")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetListRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    void getList_withoutToken_shouldReturnUnauthorized() throws Exception {
        // Arrange
        Map<String, String> params = Map.of(
                "roleNameFilter", ""
        );

        // Act
        ResultActions resultActions = performGetListRequest(null, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isUnauthorized());

    }

    @Test
    void getList_withNoFilter_shouldReturnAllRoles() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "roleNameFilter", ""
        );

        Set<Role> mockRoles = Set.of(
                new Role(1, "ROLE_ADMIN"),
                new Role(2, "ROLE_USER"),
                new Role(3, "ROLE_MODERATOR"),
                new Role(4, "ROLE_TESTER")
        );

        when(service.getList("")).thenReturn(mockRoles);

        // Act
        ResultActions resultActions = performGetListRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.roleResponseList", hasSize(4)))
                .andExpect(jsonPath("$._embedded.roleResponseList[?(@.roleName == 'ROLE_ADMIN')]").exists())
                .andExpect(jsonPath("$._embedded.roleResponseList[?(@.roleName == 'ROLE_USER')]").exists())
                .andExpect(jsonPath("$._embedded.roleResponseList[?(@.roleName == 'ROLE_MODERATOR')]").exists())
                .andExpect(jsonPath("$._embedded.roleResponseList[?(@.roleName == 'ROLE_TESTER')]").exists());

        verify(service).getList("");
    }

    @Test
    void getList_withFilter_shouldReturnModeratorRoleOnly() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        Map<String, String> params = Map.of(
                "roleNameFilter", "ODER"
        );

        Set<Role> mockRoles = Set.of(
                new Role(1, "ROLE_ADMIN"),
                new Role(2, "ROLE_USER"),
                new Role(3, "ROLE_MODERATOR"),
                new Role(4, "ROLE_TESTER")
        );

        when(service.getList("")).thenReturn(mockRoles);

        // Act
        ResultActions resultActions = performGetListRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.roleResponseList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.roleResponseList[?(@.roleName == 'ROLE_MODERATOR')]").exists());
        verify(service).getList("ODER");
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
                "sortBy", "roleName",
                "sortOrder", "asc",
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
                "sortBy", "roleName",
                "sortOrder", "asc",
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
                "sortBy", "roleName",
                "sortOrder", "asc",
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
                "sortBy", "roleId",
                "sortOrder", "desc",
                "roleNameFilter", "MODERATOR"
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
                "sortBy", "roleName",
                "sortOrder", "asc",
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
                "sortBy", "roleId",
                "sortOrder", "asc",
                "roleNameFilter", "ROLE_MODERATOR"
        );

        Page<Role> filteredPage = new PageImpl<>(List.of(testData.get(1))); // Filtered ROLE_MODERATOR
        when(service.getPage(0, 10, "roleId", "asc", "ROLE_MODERATOR"))
                .thenReturn(filteredPage);

        // Act
        ResultActions resultActions = performGetPageRequest(token, "nebula_rest_api", params);

        // Assert
        resultActions.andExpect(status().isOk());
        verify(service).getPage(0, 10, "roleId", "asc", "ROLE_MODERATOR");
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

        Role roleToCreate = new Role(77, "ROLE_SUPERVISOR");

        // Act
        performAddRequest(token, "nebula_rest_api", roleToCreate)
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

        Role roleToCreate = new Role(77, "ROLE_SUPERVISOR");

        // Act
        performAddRequest(token, "invalid_app_name", roleToCreate)
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

        Role roleToCreate = new Role(77, "ROLE_SUPERVISOR");

        // Act
        performAddRequest(token, "nebula_rest_api", roleToCreate)
                // Assert
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

        int roleId = 77;
        Role roleToUpdate = new Role(4, "ROLE_SUPERVISOR");

        // Act
        performUpdateRequest(token, "nebula_rest_api", roleId, roleToUpdate)
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value(roleId))
                .andExpect(jsonPath("$.roleName").value("ROLE_UPDATER"));

    }

    @Test
    void update_withJwtTokenAndAdminRole_shouldReturnForbidden_forInvalidAppHeader() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"),
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        int roleId = 4;
        Role roleToUpdate = new Role(77, "ROLE_SUPERVISOR");

        // Act
        performUpdateRequest(token, "invalid_app_name", roleId, roleToUpdate)
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void update_withJwtTokenAndWithoutAdminRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(2, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        int roleId = 4;
        Role roleToUpdate = new Role(77, "ROLE_SUPERVISOR");

        // Act
        performUpdateRequest(token, "nebula_rest_api", roleId, roleToUpdate)
                // Assert
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

        int roleId = 124;
        Role roleToUpdate = new Role(77, "ROLE_SUPERVISOR");

        when(service.update(eq(124), any())).thenReturn(null);

        // Act
        performUpdateRequest(token, "nebula_rest_api", roleId, roleToUpdate)
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

        when(service.delete(roleId)).thenReturn(true);

        // Act
        performDeleteRequest(token, "nebula_rest_api", roleId)
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

        // Act
        performDeleteRequest(token, "nebula_rest_api", roleId)
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

        // Act
        performDeleteRequest(token, "invalid_app_name", roleId)
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

        when(service.delete(roleId)).thenReturn(false);

        // Act
        performDeleteRequest(token, "nebula_rest_api", roleId)
                // Assert
                .andExpect(status().isNotFound());
    }

}
