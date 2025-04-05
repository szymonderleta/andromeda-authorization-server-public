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
import pl.derleta.authorization.controller.assembler.AccessTokenModelAssembler;
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.domain.request.TokenRequest;
import pl.derleta.authorization.domain.response.AccessTokenResponse;
import pl.derleta.authorization.service.token.AccessTokenService;


/**
 * Rest controller for managing access tokens. Provides endpoints for fetching,
 * creating, and deleting access tokens with features such as pagination,
 * filtering, and sorting. Access to endpoints is role-based as defined in
 * the annotations.
 */
@RestController
@RequestMapping("/api/v1")
public class AccessTokenController {

    public static final String DEFAULT_PATH = "table/tokens/access";

    private final AccessTokenService service;
    private final AccessTokenModelAssembler tokenModelAssembler;
    private final PagedResourcesAssembler<AccessToken> pagedResourcesAssembler;

    @Autowired
    public AccessTokenController(AccessTokenService service, AccessTokenModelAssembler tokenModelAssembler, PagedResourcesAssembler<AccessToken> pagedResourcesAssembler) {
        this.service = service;
        this.tokenModelAssembler = tokenModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * Fetches a paginated list of access tokens based on the provided filters and sorting options.
     *
     * @param page           the page number to retrieve, defaults to 0 if not specified
     * @param size           the number of items per page, defaults to the application's default page size
     * @param sortBy         the field by which to sort the results, defaults to "userId"
     * @param sortOrder      the sort order, either "asc" or "desc", defaults to "asc"
     * @param usernameFilter an optional filter for matching by username, defaults to an empty string
     * @param emailFilter    an optional filter for matching by email, defaults to an empty string
     * @return a ResponseEntity containing the paginated model of access token responses
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER')")
    @GetMapping(value = "/" + DEFAULT_PATH, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<AccessTokenResponse>> getPage(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "" + AndromedaAuthorizationServerApplication.DEFAULT_PAGE_SIZE) int size,
                                                                   @RequestParam(defaultValue = "userId") String sortBy,
                                                                   @RequestParam(defaultValue = "asc") String sortOrder,
                                                                   @RequestParam(defaultValue = "") String usernameFilter,
                                                                   @RequestParam(defaultValue = "") String emailFilter) {
        Page<AccessToken> tokensPage = service.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);
        PagedModel<AccessTokenResponse> model = pagedResourcesAssembler.toModel(tokensPage, tokenModelAssembler);
        return ResponseEntity.ok(model);
    }

    /**
     * Retrieves a paginated and sorted list of valid access tokens.
     *
     * @param page      the page number to retrieve, starting from 0 as the first page. Defaults to 0 if not provided.
     * @param size      the number of items per page. Defaults to the application-defined default page size if not provided.
     * @param sortBy    the field by which to sort the results. Defaults to "userId" if not provided.
     * @param sortOrder the order of sorting, either "asc" for ascending or "desc" for descending. Defaults to "asc" if not provided.
     * @return a ResponseEntity containing a PagedModel of AccessTokenResponse objects representing the valid access tokens.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/valid", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<AccessTokenResponse>> getValid(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "" + AndromedaAuthorizationServerApplication.DEFAULT_PAGE_SIZE) int size,
                                                                    @RequestParam(defaultValue = "userId") String sortBy,
                                                                    @RequestParam(defaultValue = "asc") String sortOrder) {
        Page<AccessToken> tokensPage = service.getValid(page, size, sortBy, sortOrder);
        if (tokensPage == null) {
            throw new IllegalArgumentException("Page must not be null");
        }
        PagedModel<AccessTokenResponse> model = pagedResourcesAssembler.toModel(tokensPage, tokenModelAssembler);
        return ResponseEntity.ok(model);
    }

    /**
     * Retrieves an access token response for the given ID.
     *
     * @param id the identifier of the access token to retrieve
     * @return a {@link ResponseEntity} containing the access token response and the HTTP status
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AccessTokenResponse> get(@PathVariable Integer id) {
        AccessToken accessToken = service.get(id);
        if (accessToken == null) {
            return ResponseEntity.notFound().build();
        }
        AccessTokenResponse response = tokenModelAssembler.toModel(accessToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Creates and adds a new access token for a given user.
     *
     * @param userId  The ID of the user for whom the access token will be generated.
     * @param request The token request containing the token details.
     * @return A ResponseEntity containing the created AccessTokenResponse and an HTTP CREATED status,
     * or an HTTP NOT FOUND status if the access token could not be created.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR')")
    @PostMapping("/" + DEFAULT_PATH + "/{userId}")
    public ResponseEntity<AccessTokenResponse> add(@PathVariable Long userId, @RequestBody TokenRequest request) {
        String token = request.getToken();
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        AccessToken accessToken = service.save(userId, token);
        if (accessToken == null) {
            return ResponseEntity.notFound().build();
        }
        AccessTokenResponse response = tokenModelAssembler.toModel(accessToken);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Deletes an access token associated with a specific user.
     *
     * @param tokenId the unique identifier of the access token to delete
     * @param userId  the unique identifier of the user associated with the access token
     * @return a {@code ResponseEntity} indicating the result of the operation:
     * {@code HttpStatus.OK} if the deletion was successful,
     * {@code HttpStatus.NOT_FOUND} if the token or user was not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/" + DEFAULT_PATH + "/{tokenId}/{userId}")
    public ResponseEntity<Void> delete(@PathVariable Long tokenId, @PathVariable Long userId) {
        boolean success = service.delete(tokenId, userId);
        return success ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
