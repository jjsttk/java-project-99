package hexlet.code.app.service.user;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.BaseService;
import hexlet.code.app.utils.ExceptionMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public final class UserService implements BaseService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserMapper mapper;


    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(mapper::map)
                .toList();
    }

    public UserDTO getById(Long id) {
        var entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(User.class, id)));
        return mapper.map(entity);
    }

    public UserDTO create(UserCreateDTO createDTO) {
        var entity = mapper.map(createDTO);
        var saved = userRepository.save(entity);
        return mapper.map(entity);
    }

    public UserDTO update(UserUpdateDTO updateDTO, Long id) {
        var entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(User.class, id)));
        mapper.update(updateDTO, entity);
        userRepository.save(entity);
        return mapper.map(entity);
    }

    public Long totalCount() {
        return userRepository.count();
    }

    public void delete(Long id) {
        var hasTasks = taskRepository.existsByAssigneeId(id);
        if (hasTasks) {
            throw new IllegalStateException("Cannot delete user with id = " + id
                    + " ,because user was assigned to at least one task.");
        }
        userRepository.deleteById(id);
    }
}
