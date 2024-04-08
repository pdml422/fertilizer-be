package vn.edu.usth.service.file;

import jakarta.ws.rs.core.Response;
import vn.edu.usth.model.Image;

import java.util.List;

public interface IImageService {
    public Image getImageFromId(int id);
    public List<Image> getImageFromUserId(int userId);
    public List<Image> getHeaderFromUserId(int userId);
    public Response deleteImage(int imageId);
}
