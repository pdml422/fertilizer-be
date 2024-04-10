package vn.edu.usth.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import vn.edu.usth.model.Image;

import java.util.List;

@ApplicationScoped
public class ImageRepository implements PanacheRepository<Image> {
    public Image getImageFromId(int id) {
        return find("id", id).firstResult();
    }

    public List<Image> getImageFromUserId(int userId) {
        return find("userId", userId).list();
    }

    public List<Image> getHeaderFromUserId(int userId) {
        return find("userId = ?1 and type = ?2", userId, "hdr").list();
    }
}
