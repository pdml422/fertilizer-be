package vn.edu.usth.service.image;

import jakarta.ws.rs.core.Response;
import vn.edu.usth.model.Image;

import java.util.List;

public interface IImageService {
    public Image getImageFromId(int id);
    public List<Image> getImageFromUserId(int userId);
    public List<Image> getHyperHeaderFromUserId(int userId);
    public Response deleteImage(int imageId);
}
