package vn.edu.usth.service.user;

import vn.edu.usth.exception.ResourceNotFoundException;
import vn.edu.usth.model.User;

public interface IUserService {
    User getUserById(int id) throws ResourceNotFoundException;
}
