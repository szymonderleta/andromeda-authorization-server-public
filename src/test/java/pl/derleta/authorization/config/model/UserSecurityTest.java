package pl.derleta.authorization.config.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSecurityTest {

    @Test
    void getAuthorities_withRolesPresent_shouldReturnCorrectAuthorities() {
        // Arrange
        Set<RoleSecurity> roles = new HashSet<>();
        roles.add(new RoleSecurity(1, "ROLE_ADMIN"));
        roles.add(new RoleSecurity(2, "ROLE_USER"));
        UserSecurity userSecurity = new UserSecurity(1L, "John", "john@example.com", "password", roles);

        // Act
        Collection<? extends GrantedAuthority> authorities = userSecurity.getAuthorities();

        // Assert
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getAuthorities_withoutRoles_shouldReturnEmptyAuthorities() {
        // Arrange
        Set<RoleSecurity> roles = new HashSet<>();
        UserSecurity userSecurity = new UserSecurity(1L, "John", "john@example.com", "password", roles);

        // Act
        Collection<? extends GrantedAuthority> authorities = userSecurity.getAuthorities();

        // Assert
        assertTrue(authorities.isEmpty());
    }

    @Test
    void getAuthorities_withDuplicateRoles_shouldNotDuplicateAuthorities() {
        // Arrange
        Set<RoleSecurity> roles = new HashSet<>();
        roles.add(new RoleSecurity(1, "ROLE_USER"));
        roles.add(new RoleSecurity(2, "ROLE_USER")); // Duplicate role by name
        UserSecurity userSecurity = new UserSecurity(1L, "John", "john@example.com", "password", roles);

        // Act
        Collection<? extends GrantedAuthority> authorities = userSecurity.getAuthorities();

        // Assert
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

}
