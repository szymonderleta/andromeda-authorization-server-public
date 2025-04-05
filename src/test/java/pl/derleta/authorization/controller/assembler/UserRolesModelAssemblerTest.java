package pl.derleta.authorization.controller.assembler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import pl.derleta.authorization.controller.UserRolesController;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.domain.response.RoleResponse;
import pl.derleta.authorization.domain.response.UserResponse;
import pl.derleta.authorization.domain.response.UserRolesResponse;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserRolesModelAssemblerTest {

    private UserRolesModelAssembler assembler;

    @Mock
    private UserModelAssembler userModelAssembler;

    @Mock
    private RoleModelAssembler roleModelAssembler;

    @BeforeEach
    void setUp() {
        assembler = new UserRolesModelAssembler(userModelAssembler, roleModelAssembler);
    }

    @Test
    void toModel_ShouldConvertEntityToModelAndAddSelfLink() {
        // Arrange
        User mockUser = Mockito.mock(User.class);
        Role mockRole = Mockito.mock(Role.class);

        UserRoles userRoles = new UserRoles(mockUser, new HashSet<>(Collections.singletonList(mockRole)));

        UserResponse mockUserResponse = Mockito.mock(UserResponse.class);
        Mockito.when(userModelAssembler.toModel(Mockito.eq(mockUser))).thenReturn(mockUserResponse);

        RoleResponse mockRoleResponse = Mockito.mock(RoleResponse.class);
        Mockito.when(roleModelAssembler.toModel(Mockito.eq(mockRole))).thenReturn(mockRoleResponse);

        // Act
        UserRolesResponse response = assembler.toModel(userRoles);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUser()).isEqualTo(mockUserResponse);
        assertThat(response.getRoles()).containsExactly(mockRoleResponse);

        Link selfLink = WebMvcLinkBuilder.linkTo(UserRolesController.class)
                .slash(UserRolesController.DEFAULT_PATH)
                .slash(mockUserResponse.getUserId())
                .withSelfRel();
        assertThat(response.getLinks()).contains(selfLink);
    }

}
