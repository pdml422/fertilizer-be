package vn.edu.usth.service.image;

import jakarta.ws.rs.core.Response;
import vn.edu.usth.dto.image.RGBImageRequest;
import vn.edu.usth.model.Image;

import java.util.List;

public interface IImageService {
    public Image getImageFromId(int id);
    public List<Image> getImageFromUserId(int userId);
    public List<Image> getHyperHeaderFromUserId(int userId);
    public Response deleteImage(int imageId);
    public List<Image> getMultiImageFromUserId(int userId);
    public Response getRGBImageFromHyper(RGBImageRequest request, int userId);
    public Response getRGBImageFromMulti(RGBImageRequest request, int userId);
}
