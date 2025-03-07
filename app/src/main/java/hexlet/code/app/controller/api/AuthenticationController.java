package hexlet.code.app.controller.api;

import hexlet.code.app.dto.security.AuthRequest;
import hexlet.code.app.service.security.AuthenticationService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Log4j2
public class AuthenticationController {
    private AuthenticationService authenticationService;

    /**
     * Authenticate a user and return a token.
     *
     * @param authRequest the authentication request containing user credentials
     * @return a token as a {@link String} if authentication is successful
     */
    @PostMapping("/login")
    public String authenticate(@RequestBody AuthRequest authRequest) {
        var token = authenticationService.authenticateAndGetToken(authRequest);
        return token;
    }
}
