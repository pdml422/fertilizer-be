package vn.edu.usth.dto.auth;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginRequest {
    public String username;
    public String password;
}