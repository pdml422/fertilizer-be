package vn.edu.usth.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import vn.edu.usth.model.User;

@Getter
@Setter
public class UserDto {
    @NotBlank
    @Schema(name = "email", required = true)
    private String email;
    @NotBlank
    @Schema(name = "username", required = true)
    private String username;
    @NotBlank
    @Schema(name = "password", required = true)
    private String password;
    @NotBlank
    @Schema(name = "name", required = true)
    private String name;
    @NotBlank
    @Schema(name = "role")
    private String role;

    public User toUser() {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setUsername(username);
        return user;
    }
}
