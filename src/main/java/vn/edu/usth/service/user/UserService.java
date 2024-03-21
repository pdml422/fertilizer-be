package vn.edu.usth.service.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import vn.edu.usth.exception.ResourceNotFoundException;
import vn.edu.usth.model.User;
import vn.edu.usth.repository.UserRepository;

import java.util.List;

@ApplicationScoped
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserById(int userId) throws ResourceNotFoundException {
        User user = userRepository.findById((long) userId);

        if (user == null) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.listAll();
    }

    @Override
    public User getUserByUsername(String username) throws ResourceNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User", "username", username);
        }
        return user;
    }

    @Override
    @Transactional
    public User updateUser(int id, User user) throws ResourceNotFoundException {
        User u = getUserById(id);
        u.setEmail(user.getEmail());
        u.setName(user.getName());
        u.setRole(user.getRole());
        return u;
    }

    @Override
    @Transactional
    public void deleteUser(int id) throws ResourceNotFoundException {
        userRepository.delete(getUserById(id));
    }

}
