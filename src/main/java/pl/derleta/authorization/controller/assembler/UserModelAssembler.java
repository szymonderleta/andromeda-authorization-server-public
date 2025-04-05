package pl.derleta.authorization.controller.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.controller.UserController;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.response.UserResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * The UserModelAssembler is a Spring component that extends the
 * {@link RepresentationModelAssemblerSupport} to convert {@link User} entities
 * into {@link UserResponse} HATEOAS-compliant models.
 * This assembler simplifies the process of adding HATEOAS links to the output models.
 */
@Component
public class UserModelAssembler extends RepresentationModelAssemblerSupport<User, UserResponse> {

    public UserModelAssembler() {
        super(UserController.class, UserResponse.class);
    }

    /**
     * Converts a {@link User} entity into a {@link UserResponse} model, copying
     * all relevant properties and adding a self-referential HATEOAS link to the model.
     *
     * @param entity the {@link User} entity to be converted into a {@link UserResponse} model
     * @return the {@link UserResponse} model with copied properties and an added self-referential link
     */
    @Override
    public UserResponse toModel(User entity) {
        UserResponse model = new UserResponse();
        BeanUtils.copyProperties(entity, model);
        Link selfLink = linkTo(UserController.class).slash(UserController.DEFAULT_PATH).slash(model.getUserId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

    /**
     * Converts a {@link User} entity into a {@link UserResponse} model, adding
     * a self-referential HATEOAS link to the generated response model.
     *
     * @param entity the {@link User} entity to be converted into a response model
     * @param path   the base path used to construct the self-referential HATEOAS link
     * @return a {@link UserResponse} model with properties copied from the entity
     * and a self-referential HATEOAS link added
     */
    @Deprecated
    public UserResponse toModel(User entity, String path) {
        UserResponse model = new UserResponse();
        BeanUtils.copyProperties(entity, model);
        Link selfLink = linkTo(UserController.class).slash(path).slash(model.getUserId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

}
