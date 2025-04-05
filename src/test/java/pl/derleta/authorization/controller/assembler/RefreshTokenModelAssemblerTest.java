package pl.derleta.authorization.controller.assembler;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import pl.derleta.authorization.controller.RefreshTokenController;
import pl.derleta.authorization.controller.mapper.UserApiMapper;
import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.response.RefreshTokenResponse;
import pl.derleta.authorization.domain.response.UserResponse;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class RefreshTokenModelAssemblerTest {

    private final RefreshTokenModelAssembler assembler = new RefreshTokenModelAssembler();

    @Test
    void testToModel_withValidRefreshToken_createsExpectedRefreshTokenResponse() {
        try (MockedStatic<UserApiMapper> mockedStatic = Mockito.mockStatic(UserApiMapper.class)) {
            // Arrange
            User user = new User(1L, "testuser", "Test", "User");
            String token = "sample-token";
            Timestamp expiration = Timestamp.valueOf("2024-01-01 12:00:00");
            RefreshToken refreshToken = new RefreshToken(10L, token, user, expiration);

            UserResponse userResponse = new UserResponse();
            userResponse.setUserId(user.userId());
            userResponse.setUsername(user.username());
            userResponse.setEmail(user.email());

            mockedStatic.when(() -> UserApiMapper.toUserResponse(user)).thenReturn(userResponse);

            // Act
            RefreshTokenResponse response = assembler.toModel(refreshToken);

            // Assert
            assertThat(response.getTokenId()).isEqualTo(refreshToken.tokenId());
            assertThat(response.getToken()).isEqualTo(refreshToken.token());
            assertThat(response.getExpirationDate()).isEqualTo(refreshToken.expirationDate());
            assertThat(response.getUser()).isEqualTo(userResponse);

            Link selfLink = WebMvcLinkBuilder.linkTo(RefreshTokenController.class)
                    .slash(RefreshTokenController.DEFAULT_PATH)
                    .slash(refreshToken.tokenId())
                    .withSelfRel();
            assertThat(response.getLinks()).containsExactly(selfLink);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test zakończony wyjątkiem: " + e.getMessage());
        }
    }

    @Test
    void testToModel_withNullUser_createsResponseWithNullUser() {
        try (MockedStatic<UserApiMapper> mockedStatic = Mockito.mockStatic(UserApiMapper.class)) {
            // Arrange
            String token = "sample-token";
            Timestamp expiration = Timestamp.valueOf("2024-01-01 12:00:00");
            RefreshToken refreshToken = new RefreshToken(20L, token, null, expiration);

            mockedStatic.when(() -> UserApiMapper.toUserResponse(null)).thenReturn(null);

            // Act
            RefreshTokenResponse response = assembler.toModel(refreshToken);

            // Assert
            assertThat(response.getTokenId()).isEqualTo(refreshToken.tokenId());
            assertThat(response.getToken()).isEqualTo(refreshToken.token());
            assertThat(response.getExpirationDate()).isEqualTo(refreshToken.expirationDate());
            assertThat(response.getUser()).isNull();

            Link selfLink = WebMvcLinkBuilder.linkTo(RefreshTokenController.class)
                    .slash(RefreshTokenController.DEFAULT_PATH)
                    .slash(refreshToken.tokenId())
                    .withSelfRel();
            assertThat(response.getLinks()).containsExactly(selfLink);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test was ended with exception: " + e.getMessage());
        }
    }

}
