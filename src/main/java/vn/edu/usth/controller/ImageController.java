package vn.edu.usth.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import vn.edu.usth.dto.image.RGBImageFromHyperRequest;
import vn.edu.usth.exception.ResourceNotFoundException;
import vn.edu.usth.service.image.FileUploadService;
import vn.edu.usth.service.image.ImageService;
import vn.edu.usth.service.user.UserService;

import java.nio.file.Files;
import java.nio.file.Paths;

@RequestScoped
@Path("/image")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityScheme(securitySchemeName = "Basic Auth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class ImageController {
    @Inject
    ImageService imageService;

    @Inject
    SecurityContext securityContext;

    @Inject
    FileUploadService fileUploadService;

    @Inject
    UserService userService;

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{userId}")
    public Response getImageFromUserId(@PathParam("userId") int id) {
        return Response.ok(imageService.getImageFromUserId(id)).build();
    }

    @GET
    @RolesAllowed({"USER"})
    @Path("/hyper/{id}/header")
    public Response getHyperHeaderFromUserId(@PathParam("id") int id) {
        return Response.ok(imageService.getHyperHeaderFromUserId(id)).build();
    }

    @DELETE
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{id}")
    public Response deleteImage(@PathParam("id") int imageId) {
        return imageService.deleteImage(imageId);
    }

    @POST
    @Path("/hyper")
    @RolesAllowed({"USER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadHyper(@MultipartForm MultipartFormDataInput input, @HeaderParam("userId") int userId) {
        return Response.ok().entity(fileUploadService.uploadHyper(input, userId)).build();
    }

    @POST
    @Path("/multi")
    @RolesAllowed({"USER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadMulti(@MultipartForm MultipartFormDataInput input, @HeaderParam("userId") int userId) {
        return Response.ok().entity(fileUploadService.uploadMulti(input, userId)).build();
    }

    @GET
    @RolesAllowed({"USER"})
    @Path("/hyper/rgb")
    public Response getRGBImageFromHyper(@RequestBody RGBImageFromHyperRequest request) throws ResourceNotFoundException {
        int userId = userService.getUserByUsername(securityContext.getUserPrincipal().getName()).getId();
        if (userId != imageService.getImageFromId(request.id).getUserId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String filePath = "src/main/resources/Image/" + userId + "/Output/" +
                "hyper_" + request.id + "_" + request.red + "_" + request.green + "_" + request.blue + ".png";

        if (Files.exists(Paths.get(filePath))) {
            return Response.ok("http:/localhost:8888/src/main/resources/Image/" + userId + "/Output/" +
                    "hyper_" + request.id + "_" + request.red + "_" + request.green + "_" + request.blue + ".png").build();
        }
        return Response.ok(imageService.getRGBImageFromHyper(request, userId)).build();
    }

}
