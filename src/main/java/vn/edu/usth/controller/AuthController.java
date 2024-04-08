package vn.edu.usth.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import vn.edu.usth.dto.auth.LoginRequest;
import vn.edu.usth.dto.auth.LoginResponse;
import vn.edu.usth.dto.auth.RegisterRequest;
import vn.edu.usth.model.User;
import vn.edu.usth.repository.UserRepository;
import vn.edu.usth.util.PBKDF2Encoder;
import vn.edu.usth.util.TokenUtils;

import java.nio.file.Paths;

@Path("/auth")
public class AuthController {
    @Inject
    PBKDF2Encoder passwordEncoder;
    @Inject
    UserRepository userRepository;

    @ConfigProperty(name = "vn.edu.usth.fertilizer.jwt.duration") public Long duration;
    @ConfigProperty(name = "mp.jwt.verify.issuer") public String issuer;

    @PermitAll
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        User u = userRepository.findByUsername(loginRequest.username);
        if (u != null && u.getPassword().equals(passwordEncoder.encode(loginRequest.password))) {
            try {
                return Response.ok(new LoginResponse(TokenUtils.generateToken(u.getUsername(), u.getRole(), duration, issuer))).build();
            } catch (Exception e) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @PermitAll
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    @Transactional
    public User register(RegisterRequest registerRequest) throws Exception {

        if (userRepository.findByUsername(registerRequest.username) != null) {
            throw new Exception("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.username);
        newUser.setPassword(passwordEncoder.encode(registerRequest.password));
        newUser.setRole("USER");
        newUser.setEmail(registerRequest.email);
        newUser.setName(registerRequest.name);
        userRepository.persist(newUser);

        java.nio.file.Path path1 = Paths.get("src/main/resources/Image/" + newUser.getId());
        java.nio.file.Path path2 = Paths.get("src/main/resources/Image/" + newUser.getId() + "/Hyper_spectral");
        java.nio.file.Path path3 = Paths.get("src/main/resources/Image/" + newUser.getId() + "/Multi_spectral");
        java.nio.file.Path path4 = Paths.get("src/main/resources/Image/" + newUser.getId() + "/Output");
        try {
            java.nio.file.Files.createDirectories(path1);
            java.nio.file.Files.createDirectories(path2);
            java.nio.file.Files.createDirectories(path3);
            java.nio.file.Files.createDirectories(path4);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newUser;

    }

}
