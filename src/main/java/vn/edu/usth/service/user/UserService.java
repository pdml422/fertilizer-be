package vn.edu.usth.service.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import vn.edu.usth.exception.ResourceNotFoundException;
import vn.edu.usth.model.User;
import vn.edu.usth.repository.UserRepository;

@ApplicationScoped
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(int userId) throws ResourceNotFoundException {
        User user = userRepository.findById((long) userId);

        if (user == null) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return user;
    }
}
