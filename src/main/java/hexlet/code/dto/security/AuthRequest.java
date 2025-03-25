package hexlet.code.dto.security;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
