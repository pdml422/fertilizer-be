package vn.edu.usth.service.user;

import vn.edu.usth.exception.ResourceNotFoundException;
import vn.edu.usth.model.User;

import java.util.List;

public interface IUserService {
    User getUserById(int id) throws ResourceNotFoundException;

    List<User> getAllUsers();

    User getUserByUsername(String username) throws ResourceNotFoundException;

    User updateUser(int id, User user) throws ResourceNotFoundException;

    void deleteUser(int id) throws ResourceNotFoundException;
}
