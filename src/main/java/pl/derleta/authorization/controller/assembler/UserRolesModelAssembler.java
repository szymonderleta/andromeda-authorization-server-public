package pl.derleta.authorization.controller.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.controller.UserRoleController;
import pl.derleta.authorization.controller.UserRolesController;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.domain.response.RoleResponse;
import pl.derleta.authorization.domain.response.UserResponse;
import pl.derleta.authorization.domain.response.UserRolesResponse;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * A model assembler responsible for converting {@code UserRoles} entities into their respective
 * {@code UserRolesResponse} representation models, while adding HATEOAS links for resource navigation.
 * Provides methods for mapping data from the entity to the response model and generating self-links
 * using relevant API paths.
 */
@Component
public class UserRolesModelAssembler extends RepresentationModelAssemblerSupport<UserRoles, UserRolesResponse> {

    private final UserModelAssembler userModelAssembler;
    private final RoleModelAssembler roleModelAssembler;

    public UserRolesModelAssembler(UserModelAssembler userModelAssembler, RoleModelAssembler roleModelAssembler) {
        super(UserRoleController.class, UserRolesResponse.class);
        this.userModelAssembler = userModelAssembler;
        this.roleModelAssembler = roleModelAssembler;
    }

    /**
     * Converts a {@code UserRoles} entity into a {@code UserRolesResponse} model by mapping the entity's
     * data, adding a self-link pointing to the resource's API path, and returning the resulting model.
     *
     * @param entity the {@code UserRoles} entity to be converted
     * @return a {@code UserRolesResponse} model containing the entity's data and a self-link
     */
    @Override
    public UserRolesResponse toModel(UserRoles entity) {
        UserRolesResponse model = toUserRolesModel(entity);
        Link selfLink = linkTo(UserRolesController.class).slash(UserRolesController.DEFAULT_PATH).slash(model.getUser().getUserId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

    /**
     * Converts a {@code UserRoles} entity into a {@code UserRolesResponse} model and adds a self-link
     * based on the provided API path.
     *
     * @param entity the {@code UserRoles} entity to convert
     * @param path   the specific path under which the resource will be accessible
     * @return a {@code UserRolesResponse} model containing the converted entity data and the self-link
     */
    @Deprecated
    public UserRolesResponse toModel(UserRoles entity, String path) {
        UserRolesResponse model = toUserRolesModel(entity);
        Link selfLink = linkTo(UserRoleController.class).slash(path).slash(model.getUser().getUserId()).withSelfRel();
        model.add(selfLink);
        return model;
    }

    /**
     * Converts a {@code UserRoles} entity into a {@code UserRolesResponse} model by copying properties
     * and mapping nested entities.
     *
     * @param entity the {@code UserRoles} entity to convert
     * @return a {@code UserRolesResponse} model containing the entity's data and additional nested mappings
     */
    private UserRolesResponse toUserRolesModel(UserRoles entity) {
        UserRolesResponse model = new UserRolesResponse();
        BeanUtils.copyProperties(entity, model);
        UserResponse userResponse = userModelAssembler.toModel(entity.user());
        model.setUser(userResponse);
        Set<RoleResponse> roleResponseSet = new HashSet<>();
        for (var item : entity.roles()) {
            roleResponseSet.add(
                    roleModelAssembler.toModel(item)
            );
        }
        model.setRoles(roleResponseSet);
        return model;
    }

}
