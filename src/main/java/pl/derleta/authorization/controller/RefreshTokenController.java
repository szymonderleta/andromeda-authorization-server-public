package pl.derleta.authorization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.derleta.authorization.AndromedaAuthorizationServerApplication;
import pl.derleta.authorization.controller.assembler.RefreshTokenModelAssembler;
import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.domain.request.TokenRequest;
import pl.derleta.authorization.domain.response.RefreshTokenResponse;
import pl.derleta.authorization.service.token.RefreshTokenService;


/**
 * Controller to manage refresh tokens. Provides endpoints for retrieving, adding, and deleting refresh tokens.
 * It restricts access to certain roles and supports operations such as pagination, filtering, and sorting.
 * The default base path for this controller is "/api/v1/table/tokens/refresh".
 */
@RestController
@RequestMapping("/api/v1")
public class RefreshTokenController {

    public static final String DEFAULT_PATH = "table/tokens/refresh";

    private final RefreshTokenService service;
    private final RefreshTokenModelAssembler tokenModelAssembler;
    private final PagedResourcesAssembler<RefreshToken> pagedResourcesAssembler;

    @Autowired
    public RefreshTokenController(RefreshTokenService service, RefreshTokenModelAssembler tokenModelAssembler, PagedResourcesAssembler<RefreshToken> pagedResourcesAssembler) {
        this.service = service;
        this.tokenModelAssembler = tokenModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * Retrieves a paginated and filtered list of refresh tokens.
     *
     * @param page           the page number to retrieve, default is 0
     * @param size           the number of items per page, default is the application-defined page size
     * @param sortBy         the field by which to sort the results, default is "userId"
     * @param sortOrder      the order of sorting, either "asc" (ascending) or "desc" (descending), default is "asc"
     * @param usernameFilter a filter applied to the username field, default is an empty string (no filtering)
     * @param emailFilter    a filter applied to the email field, default is an empty string (no filtering)
     * @return a {@link ResponseEntity} containing a {@link PagedModel} of {@link RefreshTokenResponse}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER')")
    @GetMapping(value = "/" + DEFAULT_PATH, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<RefreshTokenResponse>> getPage(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "" + AndromedaAuthorizationServerApplication.DEFAULT_PAGE_SIZE) int size,
                                                                    @RequestParam(defaultValue = "userId") String sortBy,
                                                                    @RequestParam(defaultValue = "asc") String sortOrder,
                                                                    @RequestParam(defaultValue = "") String usernameFilter,
                                                                    @RequestParam(defaultValue = "") String emailFilter) {
        Page<RefreshToken> tokensPage = service.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);
        PagedModel<RefreshTokenResponse> model = pagedResourcesAssembler.toModel(tokensPage, tokenModelAssembler);
        return ResponseEntity.ok(model);
    }

    /**
     * Retrieves a paginated and sorted list of valid refresh tokens.
     *
     * @param page      the page number to retrieve; default value is 0.
     * @param size      the number of records per page; default value is the application-defined page size.
     * @param sortBy    the attribute to sort the results by; default value is "userId".
     * @param sortOrder the order to sort the results in; either "asc" for ascending or "desc" for descending; default is "asc".
     * @return a ResponseEntity containing a PagedModel of RefreshTokenResponse representing the paginated and sorted valid refresh tokens.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/valid", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<RefreshTokenResponse>> getValid(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "" + AndromedaAuthorizationServerApplication.DEFAULT_PAGE_SIZE) int size,
                                                                     @RequestParam(defaultValue = "userId") String sortBy,
                                                                     @RequestParam(defaultValue = "asc") String sortOrder) {
        Page<RefreshToken> tokensPage = service.getValid(page, size, sortBy, sortOrder);
        if (tokensPage == null) {
            throw new IllegalArgumentException("Page must not be null");
        }
        PagedModel<RefreshTokenResponse> model = pagedResourcesAssembler.toModel(tokensPage, tokenModelAssembler);
        return ResponseEntity.ok(model);
    }

    /**
     * Retrieves a refresh token response for the given identifier.
     *
     * @param id the unique identifier of the refresh token to retrieve
     * @return a {@link ResponseEntity} containing the refresh token response and an HTTP status code
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RefreshTokenResponse> get(@PathVariable Integer id) {
        RefreshToken refreshToken = service.get(id);
        if (refreshToken == null) {
            return ResponseEntity.notFound().build();
        }
        RefreshTokenResponse response = tokenModelAssembler.toModel(refreshToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Adds a new refresh token for the specified user based on the provided token request.
     *
     * @param userId  the ID of the user for whom the token is being added
     * @param request the token request containing the token string to be saved
     * @return a ResponseEntity containing a RefreshTokenResponse if the operation is
     * successful with HTTP status 201 CREATED;
     * HTTP status 400 BAD REQUEST if the token is invalid or null;
     * HTTP status 404 NOT FOUND if the token could not be saved
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR')")
    @PostMapping("/" + DEFAULT_PATH + "/{userId}")
    public ResponseEntity<RefreshTokenResponse> add(@PathVariable Long userId, @RequestBody TokenRequest request) {
        String token = request.getToken();
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        RefreshToken refreshToken = service.save(userId, token);
        if (refreshToken == null) {
            return ResponseEntity.notFound().build();
        }
        RefreshTokenResponse response = tokenModelAssembler.toModel(refreshToken);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Deletes a refresh token associated with a specific token ID and user ID.
     * This action is restricted to users with the ROLE_ADMIN role.
     *
     * @param tokenId the ID of the refresh token to be deleted
     * @param userId  the ID of the user associated with the refresh token
     * @return a {@code ResponseEntity} with status {@code HttpStatus.OK} if the deletion
     * is successful, or {@code HttpStatus.NOT_FOUND} if the token is not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/" + DEFAULT_PATH + "/{tokenId}/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long tokenId, @PathVariable Long userId) {
        boolean success = service.delete(tokenId, userId);
        return success ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
