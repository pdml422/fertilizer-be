package vn.edu.usth.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import vn.edu.usth.service.file.FileUploadService;
import vn.edu.usth.service.file.ImageService;

@RequestScoped
@Path("/image")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityScheme(securitySchemeName = "Basic Auth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class ImageController {
    private final ImageService imageService;

    @Inject
    FileUploadService fileUploadService;

    @Inject
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GET
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/hyper/{id}")
    public Response getHyperFromUserId(@PathParam("id") int id) {
        return Response.ok(imageService.getImageFromUserId(id)).build();
    }

    @GET
    @RolesAllowed({"USER"})
    @Path("/hyper/{id}/header")
    public Response getHyperHeaderFromUserId(@PathParam("id") int id) {
        return Response.ok(imageService.getHeaderFromUserId(id)).build();
    }

    @POST
    @Path("/hyper")
    @RolesAllowed({"USER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fileUpload(@MultipartForm MultipartFormDataInput input, @HeaderParam("userId") int userId) {
        return Response.ok().entity(fileUploadService.uploadFile(input, userId)).build();
    }

    @DELETE
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/hyper/{id}")
    public Response deleteHyper(@PathParam("id") int imageId) {
        return imageService.deleteImage(imageId);
    }

}
