package vn.edu.usth.service.file;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import vn.edu.usth.model.Image;
import vn.edu.usth.repository.ImageRepository;

import java.io.File;
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
    public List<Image> getHeaderFromUserId(int userId) {
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

}
