package pl.derleta.authorization.controller.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Spring Security AccessDeniedException and returns forbidden status.
     *
     * @return a ResponseEntity containing the error message and HTTP status FORBIDDEN
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You do not have permission to perform this action.");
    }

    /**
     * Handles Spring Security AuthenticationException and returns unauthorized status.
     *
     * @return a ResponseEntity containing the error message and HTTP status UNAUTHORIZED
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authentication is required to access this resource.");
    }

    /**
     * Handles RuntimeException and returns a standardized error response.
     * This is the default handler for other types of runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An internal error occurred: " + ex.getMessage());
    }

}
