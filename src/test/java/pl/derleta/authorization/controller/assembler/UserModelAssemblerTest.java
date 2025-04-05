package pl.derleta.authorization.controller.assembler;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import pl.derleta.authorization.controller.UserController;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.response.UserResponse;

import static org.assertj.core.api.Assertions.assertThat;

class UserModelAssemblerTest {

    @Test
    void toModel_withValidUser_shouldConvertToUserResponseModel() {
        // Arrange
        User user = new User(1L, "testuser", "password123", "test@example.com");
        UserModelAssembler assembler = new UserModelAssembler();

        // Act
        UserResponse userResponse = assembler.toModel(user);

        // Assert
        assertThat(userResponse.getUserId()).isEqualTo(user.userId());
        assertThat(userResponse.getUsername()).isEqualTo(user.username());
        assertThat(userResponse.getEmail()).isEqualTo(user.email());
        Link expectedSelfLink = WebMvcLinkBuilder.linkTo(UserController.class)
                .slash(UserController.DEFAULT_PATH)
                .slash(user.userId())
                .withSelfRel();
        assertThat(userResponse.getLinks()).contains(expectedSelfLink);
    }

}
