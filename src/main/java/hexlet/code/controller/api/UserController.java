package hexlet.code.controller.api;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {
    private final UserService service;

    /**
     * Retrieves a list of all users.
     *
     * @return A list of UserDTO objects and the total count in the response header.
     */
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserDTO>> index() {
        var users = service.getAll();
        var count = service.totalCount();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(count))
                .body(users);
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id The ID of the user.
     * @return The UserDTO object.
     */
    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * Creates a new user.
     *
     * @param createDTO The user creation DTO containing the necessary data.
     * @return The created UserDTO object.
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@RequestBody @Valid UserCreateDTO createDTO) {
        return service.create(createDTO);
    }

    /**
     * Updates an existing user.
     *
     * @param id The ID of the user to update.
     * @param updateDTO The DTO containing updated user data.
     * @return The updated UserDTO object.
     */
    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userSecurityService.getCurrentUser().id == #id or hasRole('ADMIN')")
    public UserDTO update(@PathVariable Long id,
                          @RequestBody @Valid UserUpdateDTO updateDTO) {
        return service.update(updateDTO, id);
    }

    /**
     * Deletes a user by ID.
     *
     * @param id The ID of the user to delete.
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("@userSecurityService.getCurrentUser().id == #id or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
