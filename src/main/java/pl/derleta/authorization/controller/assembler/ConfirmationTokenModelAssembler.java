package pl.derleta.authorization.controller.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.controller.ConfirmationTokenController;
import pl.derleta.authorization.controller.mapper.UserApiMapper;
import pl.derleta.authorization.domain.model.ConfirmationToken;
import pl.derleta.authorization.domain.response.ConfirmationTokenResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * This class is responsible for converting {@link ConfirmationToken} entities into
 * {@link ConfirmationTokenResponse} models while adding HATEOAS links for RESTful API responses.
 * It extends {@link RepresentationModelAssemblerSupport} to provide specific implementation details
 * for assembling representation models of confirmation tokens.
 * <p>
 * The class handles two scenarios:
 * 1. Converting an entity into a response model and automatically adding a default self-link.
 * 2. Converting an entity into a response model while allowing customization of the HATEOAS link base path.
 * <p>
 * This assembler is typically used in conjunction with the Spring HATEOAS library to facilitate
 * the creation of hypermedia-driven REST APIs.
 * <p>
 * The returned {@link ConfirmationTokenResponse} contains:
 * - The token details
 * - Details of the associated user
 * - A self-referential link for the token resource
 * <p>
 * The HATEOAS links are generated using Spring's {@code linkTo} and {@code slash} methods, enabling
 * seamless navigation to resource endpoints.
 */
@Component
public class ConfirmationTokenModelAssembler extends RepresentationModelAssemblerSupport<ConfirmationToken, ConfirmationTokenResponse> {

    public ConfirmationTokenModelAssembler() {
        super(ConfirmationTokenController.class, ConfirmationTokenResponse.class);
    }

    /**
     * Converts a {@link ConfirmationToken} entity into a {@link ConfirmationTokenResponse} model
     * and adds a self-referential HATEOAS link pointing to the token resource.
     *
     * @param item the {@link ConfirmationToken} entity to be converted
     * @return a {@link ConfirmationTokenResponse} model containing token details, associated user details,
     * and a HATEOAS link for the resource
     */
    @Override
    public ConfirmationTokenResponse toModel(ConfirmationToken item) {
        ConfirmationTokenResponse model = new ConfirmationTokenResponse();
        BeanUtils.copyProperties(item, model);
        model.setUser(UserApiMapper.toUserResponse(item.user()));
        Link selfLink = linkTo(ConfirmationTokenController.class).slash(ConfirmationTokenController.DEFAULT_PATH).slash(model.getTokenId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

    /**
     * Converts a {@link ConfirmationToken} entity into a {@link ConfirmationTokenResponse} model
     * and adds a self-referential HATEOAS link pointing to the token resource.
     *
     * @param item the {@link ConfirmationToken} entity to be converted
     * @param path the base path used to generate the HATEOAS self-link
     * @return a {@link ConfirmationTokenResponse} model containing token details and a HATEOAS link for the resource
     */
    @Deprecated
    public ConfirmationTokenResponse toModel(ConfirmationToken item, String path) {
        ConfirmationTokenResponse model = new ConfirmationTokenResponse();
        BeanUtils.copyProperties(item, model);
        Link selfLink = linkTo(ConfirmationTokenController.class).slash(path).slash(model.getTokenId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

}
