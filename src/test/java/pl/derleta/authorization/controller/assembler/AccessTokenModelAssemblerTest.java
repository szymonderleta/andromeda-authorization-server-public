package pl.derleta.authorization.controller.assembler;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import pl.derleta.authorization.controller.AccessTokenController;
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.response.AccessTokenResponse;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenModelAssemblerTest {

    @Test
    void toModel_whenValidAccessToken_shouldMapToAccessTokenResponse() {
        // Arrange
        AccessToken accessToken = new AccessToken(1L, "sample-token",
                new User(2L, "John", "Doe", "john.doe@example.com"),
                new Timestamp(System.currentTimeMillis()));
        AccessTokenModelAssembler assembler = new AccessTokenModelAssembler();

        // Act
        AccessTokenResponse response = assembler.toModel(accessToken);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getTokenId());
        assertEquals("sample-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("John", response.getUser().getUsername());
        assertEquals("john.doe@example.com", response.getUser().getEmail());

        Link selfLink = response.getLink("self").orElse(null);
        assertNotNull(selfLink);
        assertTrue(selfLink.getHref().contains("/" + AccessTokenController.DEFAULT_PATH + "/1"));
    }

}
