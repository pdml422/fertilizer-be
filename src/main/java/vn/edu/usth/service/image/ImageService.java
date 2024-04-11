package vn.edu.usth.service.image;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import vn.edu.usth.dto.image.RGBImageFromHyperRequest;
import vn.edu.usth.model.Image;
import vn.edu.usth.repository.ImageRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

@ApplicationScoped
public class ImageService implements IImageService {
    private final ImageRepository imageRepository;

    @Inject
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Image getImageFromId(int id) {
        return imageRepository.getImageFromId(id);
    }

    @Override
    public List<Image> getImageFromUserId(int userId) {
        return imageRepository.getImageFromUserId(userId);
    }

    @Override
    public List<Image> getHyperHeaderFromUserId(int userId) {
        return imageRepository.getHeaderFromUserId(userId);
    }

    @Override
    @Transactional
    public Response deleteImage(int imageId) {
        String filePath = getImageFromId(imageId).getPath();
        File file = new File(filePath);
        if (file.delete()) {
            imageRepository.delete(getImageFromId(imageId));
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    public String getRGBImageFromHyper(RGBImageFromHyperRequest request, int userId) {
        StringBuilder res = new StringBuilder();
        try {
            String imagePath = getImageFromId(request.id).getPath();
            String pythonPath = "python src/main/resources/hyperToRGB.py "
                    + request.id + " " + userId + " " + imagePath + " " + request.red + " " + request.green + " " + request.blue;
            Process p = Runtime.getRuntime().exec(pythonPath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                res.append(line);
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return res.toString();
    }

}
