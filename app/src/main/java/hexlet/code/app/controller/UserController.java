package hexlet.code.app.controller;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("")
    public List<UserDTO> index() {
        return service.getEntities();
    }

    @GetMapping("{id}")
    public UserDTO show(@PathVariable Long id) {
        return service.getUserById(id);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@RequestBody @Valid UserCreateDTO createDTO) {
        return service.create(createDTO);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@PathVariable Long id,
                       @RequestBody @Valid UserUpdateDTO updateDTO) {
        return service.update(updateDTO, id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        var mbUser = service.getUserById(id);
        service.delete(mbUser.getId());
    }
}
