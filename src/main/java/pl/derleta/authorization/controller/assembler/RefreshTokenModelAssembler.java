package pl.derleta.authorization.controller.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.controller.RefreshTokenController;
import pl.derleta.authorization.controller.mapper.UserApiMapper;
import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.domain.response.RefreshTokenResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Assembles a {@link RefreshTokenResponse} model from a {@link RefreshToken} entity.
 * Extends {@link RepresentationModelAssemblerSupport} to add HATEOAS support for
 * resource links such as self-referential links.
 * <p>
 * This assembler is registered as a Spring component and is intended to be used
 * for mapping refresh token entities to their API response models with associated links.
 */
@Component
public class RefreshTokenModelAssembler extends RepresentationModelAssemblerSupport<RefreshToken, RefreshTokenResponse> {

    public RefreshTokenModelAssembler() {
        super(RefreshTokenController.class, RefreshTokenResponse.class);
    }

    /**
     * Converts a {@link RefreshToken} entity into a {@link RefreshTokenResponse} model.
     * Copies the properties from the given entity to the model, maps the associated user,
     * and adds a self-referential HATEOAS link to the model.
     *
     * @param item the {@link RefreshToken} entity to be converted into a {@link RefreshTokenResponse} model
     * @return a {@link RefreshTokenResponse} model populated with the properties of the given entity
     * and an added self-referential link
     */
    @Override
    public RefreshTokenResponse toModel(RefreshToken item) {
        RefreshTokenResponse model = new RefreshTokenResponse();
        BeanUtils.copyProperties(item, model);
        model.setUser(UserApiMapper.toUserResponse(item.user()));
        Link selfLink = linkTo(RefreshTokenController.class).slash(RefreshTokenController.DEFAULT_PATH).slash(model.getTokenId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

    /**
     * Converts a {@link RefreshToken} entity into a {@link RefreshTokenResponse} model,
     * copying all relevant properties and adding a self-referential HATEOAS link to the model.
     *
     * @param item the {@link RefreshToken} entity to be converted into a {@link RefreshTokenResponse} model
     * @param path the base path used to construct the self-referential HATEOAS link
     * @return the {@link RefreshTokenResponse} model populated with copied properties and an added self-referential link
     */
    @Deprecated
    public RefreshTokenResponse toModel(RefreshToken item, String path) {
        RefreshTokenResponse model = new RefreshTokenResponse();
        BeanUtils.copyProperties(item, model);
        Link selfLink = linkTo(RefreshTokenController.class).slash(path).slash(model.getTokenId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

}
