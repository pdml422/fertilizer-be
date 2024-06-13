package vn.edu.usth.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import vn.edu.usth.dto.image.CropHyperRequest;
import vn.edu.usth.dto.image.PredictHyperRequest;
import vn.edu.usth.exception.ResourceNotFoundException;
import vn.edu.usth.service.image.FileUploadService;
import vn.edu.usth.service.image.ImageService;
import vn.edu.usth.service.predict.PredictService;
import vn.edu.usth.service.user.UserService;

import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/predict")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityScheme(securitySchemeName = "Basic Auth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class PredictController {
    @Inject
    FileUploadService fileUploadService;

    @Inject
    PredictService predictService;

    @Inject
    UserService userService;

    @Inject
    SecurityContext securityContext;

    @Inject
    ImageService imageService;

    @POST
    @Path("/model")
    @RolesAllowed({"USER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadModel(@MultipartForm MultipartFormDataInput input, @HeaderParam("userId") int userId) {
        return Response.ok().entity(fileUploadService.uploadModel(input, userId)).build();
    }

    @GET
    @Path("/model/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getModelFromUserId(@PathParam("userId") int id) {
        return Response.ok(predictService.getModelFromUserId(id)).build();
    }

    @POST
    @Path("/hyper")
    @RolesAllowed({"USER"})
    public Response predictHyper(@Valid @RequestBody PredictHyperRequest request) throws ResourceNotFoundException {
        int userId = userService.getUserByUsername(securityContext.getUserPrincipal().getName()).getId();
        if (userId != imageService.getImageFromId(request.imageId).getUserId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String filePath = "src/main/resources/Image/" + userId + "/Output/" +
                "predict_hyper_" + request.imageId + "_" + request.modelId + "_" + request.x + "_" + request.y + ".png";

        if (Files.exists(Paths.get(filePath))) {
            return Response.ok("http://localhost:8888/src/main/resources/Image/" + userId + "/Output/" +
                    "predict_hyper_" + request.imageId + "_" + request.modelId + "_" + request.x + "_" + request.y + ".png").build();
        }

        return predictService.predictHyper(request, userId);
    }

    @POST
    @Path("/hyper/rgb")
    @RolesAllowed({"USER"})
    public Response cropHyper(@Valid @RequestBody CropHyperRequest request) throws ResourceNotFoundException {
        int userId = userService.getUserByUsername(securityContext.getUserPrincipal().getName()).getId();
        if (userId != imageService.getImageFromId(request.imageId).getUserId()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        String filePath = "src/main/resources/Image/" + userId + "/Output/" +
                "hyper_crop_" + request.imageId + "_" + request.x + "_" + request.y + ".png";

        if (Files.exists(Paths.get(filePath))) {
            return Response.ok("http://localhost:8888/src/main/resources/Image/" + userId + "/Output/" +
                    "hyper_crop_" + request.imageId + "_" + request.x + "_" + request.y + ".png").build();
        }

        return predictService.cropHyper(request, userId);
    }

}
