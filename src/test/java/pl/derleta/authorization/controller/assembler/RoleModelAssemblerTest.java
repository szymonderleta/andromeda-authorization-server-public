package pl.derleta.authorization.controller.assembler;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import pl.derleta.authorization.controller.RoleController;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.response.RoleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

class RoleModelAssemblerTest {

    @Test
    void toModel_withValidDefaultPath_shouldReturnCorrectRoleResponse() {
        // Arrange
        int roleId = 1;
        String roleName = "Admin";
        Role role = new Role(roleId, roleName);
        RoleModelAssembler assembler = new RoleModelAssembler();

        // Act
        RoleResponse response = assembler.toModel(role);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleId()).isEqualTo(roleId);
        assertThat(response.getRoleName()).isEqualTo(roleName);
        Link expectedLink = linkTo(RoleController.class)
                .slash(RoleController.DEFAULT_PATH)
                .slash(roleId)
                .withSelfRel();
        assertThat(response.getLinks()).contains(expectedLink);
    }

    @Test
    void toModel_withCustomPath_shouldReturnCorrectRoleResponse() {
        // Arrange
        int roleId = 2;
        String roleName = "User";
        String customPath = "custom-path";
        Role role = new Role(roleId, roleName);
        RoleModelAssembler assembler = new RoleModelAssembler();

        // Act
        RoleResponse response = assembler.toModel(role, customPath);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRoleId()).isEqualTo(roleId);
        assertThat(response.getRoleName()).isEqualTo(roleName);
        Link expectedLink = linkTo(RoleController.class)
                .slash(customPath)
                .slash(roleId)
                .withSelfRel();
        assertThat(response.getLinks()).contains(expectedLink);
    }

}
