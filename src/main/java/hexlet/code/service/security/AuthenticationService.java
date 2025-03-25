package hexlet.code.service.security;

import hexlet.code.dto.security.AuthRequest;
import hexlet.code.utils.JWTUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public final class AuthenticationService {
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public String authenticateAndGetToken(AuthRequest authRequest) {
        authenticate(authRequest);
        return generateTokenForUser(authRequest.getUsername());
    }

    private void authenticate(AuthRequest authRequest) {
        var authentication = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword());
        authenticationManager.authenticate(authentication);
    }

    private String generateTokenForUser(String username) {
        return jwtUtils.generateToken(username);
    }
}

