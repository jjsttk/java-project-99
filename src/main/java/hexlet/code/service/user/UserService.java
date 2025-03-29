package hexlet.code.service.user;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.BaseService;
import hexlet.code.utils.ExceptionMessage;
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
        userRepository.deleteById(id);
    }
}
