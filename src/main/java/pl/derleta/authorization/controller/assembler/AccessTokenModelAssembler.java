package pl.derleta.authorization.controller.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.controller.AccessTokenController;
import pl.derleta.authorization.controller.mapper.UserApiMapper;
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.domain.response.AccessTokenResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


/**
 * Assembler class responsible for converting {@link AccessToken} entities into
 * {@link AccessTokenResponse} models. This class also adds HATEOAS support by
 * attaching self-referential links to the generated models.
 */
@Component
public class AccessTokenModelAssembler extends RepresentationModelAssemblerSupport<AccessToken, AccessTokenResponse> {

    public AccessTokenModelAssembler() {
        super(AccessTokenController.class, AccessTokenResponse.class);
    }

    /**
     * Converts an {@code AccessToken} entity into an {@code AccessTokenResponse} model.
     * Copies all relevant properties from the {@code AccessToken} entity to the
     * {@code AccessTokenResponse} model, including the user information, and adds
     * a self-referential HATEOAS link to the model.
     *
     * @param item the {@code AccessToken} entity to be converted
     * @return the {@code AccessTokenResponse} model with copied properties and an added self-referential link
     */
    @Override
    public AccessTokenResponse toModel(AccessToken item) {
        AccessTokenResponse model = new AccessTokenResponse();
        BeanUtils.copyProperties(item, model);
        model.setUser(UserApiMapper.toUserResponse(item.user()));
        Link selfLink = linkTo(AccessTokenController.class).slash(AccessTokenController.DEFAULT_PATH).slash(model.getTokenId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

    /**
     * Converts an {@code AccessToken} entity into an {@code AccessTokenResponse} model.
     * Copies all relevant properties from the {@code AccessToken} entity to the
     * {@code AccessTokenResponse} model, and adds a self-referential HATEOAS link to the model.
     *
     * @param item the {@code AccessToken} entity to be converted
     * @param path the base path used to construct the self-referential HATEOAS link
     * @return the {@code AccessTokenResponse} model with copied properties and an added self-referential link
     */
    @Deprecated
    public AccessTokenResponse toModel(AccessToken item, String path) {
        AccessTokenResponse model = new AccessTokenResponse();
        BeanUtils.copyProperties(item, model);
        Link selfLink = linkTo(AccessTokenController.class).slash(path).slash(model.getTokenId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

}
