package pl.derleta.authorization.controller.assembler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import pl.derleta.authorization.controller.UserRoleController;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRole;
import pl.derleta.authorization.domain.response.RoleResponse;
import pl.derleta.authorization.domain.response.UserResponse;
import pl.derleta.authorization.domain.response.UserRoleResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserRoleModelAssemblerTest {

    private UserRoleModelAssembler assembler;

    @Mock
    private UserModelAssembler userModelAssembler;

    @Mock
    private RoleModelAssembler roleModelAssembler;

    @BeforeEach
    void setUp() {
        assembler = new UserRoleModelAssembler(userModelAssembler, roleModelAssembler);
    }

    @Test
    void toModel_validUserRole_mapsToUserRoleResponse() {
        // Arrange
        long userRoleId = 1L;
        User mockUser = Mockito.mock(User.class);
        Role mockRole = Mockito.mock(Role.class);

        UserRole userRole = new UserRole(userRoleId, mockUser, mockRole);

        UserResponse mockUserResponse = Mockito.mock(UserResponse.class);
        Mockito.when(userModelAssembler.toModel(Mockito.eq(mockUser))).thenReturn(mockUserResponse);
        Mockito.when(mockUserResponse.getUserId()).thenReturn(userRoleId);

        RoleResponse mockRoleResponse = Mockito.mock(RoleResponse.class);
        Mockito.when(roleModelAssembler.toModel(Mockito.eq(mockRole))).thenReturn(mockRoleResponse);

        // Act
        UserRoleResponse result = assembler.toModel(userRole);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(mockUserResponse);
        assertThat(result.getRole()).isEqualTo(mockRoleResponse);

        Link selfLink = WebMvcLinkBuilder.linkTo(UserRoleController.class)
                .slash(UserRoleController.DEFAULT_PATH)
                .slash(mockUserResponse.getUserId())
                .withSelfRel();
        assertThat(result.getLinks()).contains(selfLink);
    }

}
