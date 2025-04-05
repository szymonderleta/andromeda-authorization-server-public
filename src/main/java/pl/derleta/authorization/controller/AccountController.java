package pl.derleta.authorization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.derleta.authorization.controller.assembler.UserRolesModelAssembler;
import pl.derleta.authorization.domain.request.*;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.response.UserRolesResponse;
import pl.derleta.authorization.service.accounts.impl.AccountsServiceImpl;

/**
 * The AccountController class is a REST controller that provides API endpoints
 * for managing user account-related operations. It allows user registration,
 * role retrieval, account confirmation, unlocking accounts, password reset,
 * and password updates.
 * <p>
 * Endpoints provided by this controller include:
 * - User registration
 * - Role retrieval by username and email
 * - Account confirmation
 * - Unlocking user accounts
 * - Password reset
 * - Account password updates
 * <p>
 * The controller uses an {@link AccountsServiceImpl} service and
 * {@link UserRolesModelAssembler} to handle business logic and data transformation.
 */
@RestController
@RequestMapping("/api/v1")
public class AccountController {

    public static final String DEFAULT_PATH = "public/account";
    public static final String SECOND_PATH = "account";


    private final AccountsServiceImpl service;
    private final UserRolesModelAssembler userRolesModelAssembler;

    @Autowired
    public AccountController(AccountsServiceImpl service, UserRolesModelAssembler userRolesModelAssembler) {
        this.service = service;
        this.userRolesModelAssembler = userRolesModelAssembler;
    }

    /**
     * Handles user registration requests. Accepts a registration request and processes it to register a new user.
     * Returns a response indicating the result of the registration process.
     *
     * @param request The user registration request containing the necessary details for registering a new user.
     * @return A ResponseEntity containing an AccountResponse. If the registration is successful, it returns an HTTP 200 OK status
     * along with the AccountResponse. Otherwise, it returns an HTTP 500 Internal Server Error status with the AccountResponse.
     */
    @PostMapping(value = "/" + DEFAULT_PATH + "/register", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AccountResponse> register(@RequestBody UserRegistrationRequest request) {
        var response = service.register(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Retrieves the roles and related details for a user based on the provided username and email address.
     *
     * @param username the username of the user whose roles are requested
     * @param email    the email address of the user whose roles are requested
     * @return a ResponseEntity containing a UserRolesResponse object with the roles and related information of the user
     */
    @GetMapping(value = "/" + DEFAULT_PATH, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<UserRolesResponse> get(@RequestParam String username, @RequestParam String email) {
        UserRolesResponse response = userRolesModelAssembler.toModel(
                service.get(username, email)
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Confirms the user registration process or verifies a specific action based on the provided confirmation details.
     *
     * @param confirmation the request object containing the necessary confirmation details (e.g., token or verification codes)
     * @return a ResponseEntity containing an AccountResponse object, which indicates the success or failure of the operation
     */
    @PatchMapping(value = "/" + DEFAULT_PATH + "/confirm", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AccountResponse> confirm(@RequestBody UserConfirmationRequest confirmation) {
        var response = service.confirm(confirmation);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Unlocks a locked user account based on the provided user ID.
     *
     * @param id the unique identifier of the user account to be unlocked
     * @return a ResponseEntity containing an AccountResponse object, which indicates the success or failure of the operation
     */
    @PatchMapping(value = "/" + DEFAULT_PATH + "/unlock/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AccountResponse> unlock(@PathVariable Long id) {
        UserUnlockRequest request = new UserUnlockRequest(id);
        var response = service.unlock(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles the password reset process for the account associated with the specified email address.
     *
     * @param email the email address associated with the account for which the password reset is requested
     * @return a ResponseEntity containing an AccountResponse object, which indicates the success or failure of the operation
     */
    @PatchMapping(value = "/" + DEFAULT_PATH + "/reset-password/{email}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AccountResponse> resetPassword(@PathVariable String email) {
        ResetPasswordRequest request = new ResetPasswordRequest(email);
        var response = service.resetPassword(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles the request to update the account password.
     *
     * @param changePasswordRequest the request object containing current and new password details
     * @return a ResponseEntity containing an AccountResponse object, which indicates the success or failure of the operation
     */
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/" + SECOND_PATH + "/change-password", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AccountResponse> updatePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        var response = service.updatePassword(changePasswordRequest);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
