package hexlet.code.service.security;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Service that provides security-related operations for user management,
 * such as fetching the currently authenticated user.
 * <p>
 * This class provides a method to get the currently authenticated user based on the security context.
 */
@Component
@AllArgsConstructor
public final class UserSecurityService {
    private final UserRepository userRepository;

    /**
     * Retrieves the current authenticated user from the security context.
     *
     * @return the current authenticated {@link User}, or {@code null} if no authenticated user is found.
     */
    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        var mbUser = userRepository.findByEmail(email);
        return mbUser.orElse(null);
    }
}
