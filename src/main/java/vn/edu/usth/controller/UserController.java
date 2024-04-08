package vn.edu.usth.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import vn.edu.usth.dto.user.UserDto;
import vn.edu.usth.exception.ResourceNotFoundException;
import vn.edu.usth.model.User;
import vn.edu.usth.service.user.UserService;

import java.util.List;

@Path("/user")
@SecurityScheme(securitySchemeName = "Basic Auth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class UserController {
    private final UserService userService;

    @Inject
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GET
    @RolesAllowed({"ADMIN"})
    @Path("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @RolesAllowed({"ADMIN"})
    @Path("/{id}")
    public User getUserById(@PathParam("id") int id) throws ResourceNotFoundException {
        return userService.getUserById(id);
    }

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/me")
    public User getCurrentUser(@Context SecurityContext securityContext) throws ResourceNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        return userService.getUserByUsername(username);
    }

    @PUT
    @RolesAllowed({"ADMIN", "USER"})
    @Path("/{id}")
    public User updateUser(@PathParam("id") int id, @Valid UserDto userDto) throws ResourceNotFoundException {
        if (userService != null && userDto != null) {
            return userService.updateUser(id, userDto.toUser());
        } else {
            throw new IllegalArgumentException("userService or create user is null");
        }
    }

    @DELETE
    @RolesAllowed({"ADMIN"})
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id) throws ResourceNotFoundException {
        userService.deleteUser(id);
        return Response.status(Response.Status.OK).build();
    }
}
