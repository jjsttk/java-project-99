package hexlet.code.app.controller.api;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {
    private final UserService service;

    /**
     * Get a list of all users.
     *
     * @return a list of all users as {@link UserDTO}
     */
    @GetMapping("/users")
    public List<UserDTO> index() {
        return service.getEntities();
    }

    /**
     * Get user information by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return a {@link UserDTO} object representing the user
     */
    @GetMapping("/users/{id}")
    public UserDTO show(@PathVariable Long id) {
        return service.getUserById(id);
    }

    /**
     * Create a new user.
     *
     * @param createDTO the data transfer object containing the details of the user to be created
     * @return a {@link UserDTO} object representing the created user
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@RequestBody @Valid UserCreateDTO createDTO) {
        return service.create(createDTO);
    }

    /**
     * Update an existing user's data.
     *
     * @param id the ID of the user to update
     * @param updateDTO the data transfer object containing the updated details of the user
     * @return a {@link UserDTO} object representing the updated user
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("@userSecurityService.getCurrentUser().id == #id or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@PathVariable Long id,
                          @RequestBody @Valid UserUpdateDTO updateDTO) {
        return service.update(updateDTO, id);
    }

    /**
     * Delete a user.
     *
     * @param id the ID of the user to delete
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("@userSecurityService.getCurrentUser().id == #id or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        var mbUser = service.getUserById(id);
        service.delete(mbUser.getId());
    }
}
