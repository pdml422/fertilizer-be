package vn.edu.usth.service.predict;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import vn.edu.usth.dto.image.CropHyperRequest;
import vn.edu.usth.dto.image.PredictHyperRequest;
import vn.edu.usth.model.Image;
import vn.edu.usth.repository.ImageRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


@ApplicationScoped
public class PredictService {
    private final ImageRepository imageRepository;

    @Inject
    public PredictService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public List<Image> getModelFromUserId(int userId) {
        return imageRepository.getModelFromUserId(userId);
    }

    public Response predictHyper(PredictHyperRequest request, int userId) {
        StringBuilder res = new StringBuilder();
        try {
            String imagePath = imageRepository.getImageFromId(request.imageId).getPath();
            String modelPath = imageRepository.getImageFromId(request.modelId).getPath();
            String pythonPath = "python src/main/resources/predictHyper.py "
                    + imagePath + " " + modelPath + " " + request.x + " " + request.y + " " + userId + " " + request.imageId + " " + request.modelId;
            Process p = Runtime.getRuntime().exec(pythonPath);
            if (p.waitFor() != 0) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                res.append(line);
            }
            reader.close();

            return Response.ok(res.toString()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response cropHyper(CropHyperRequest request, int userId) {
        StringBuilder res = new StringBuilder();
        try {
            String imagePath = imageRepository.getImageFromId(request.imageId).getPath();
            String pythonPath = "python src/main/resources/cropHyper.py "
                    + request.imageId + " " + userId + " " + imagePath + " " + request.x + " " + request.y;
            Process p = Runtime.getRuntime().exec(pythonPath);
            if (p.waitFor() != 0) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                res.append(line);
            }
            reader.close();

            return Response.ok(res.toString()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
