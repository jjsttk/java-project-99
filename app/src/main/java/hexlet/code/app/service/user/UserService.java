package hexlet.code.app.service.user;


import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.utils.ExceptionMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public final class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    public UserDTO getUserById(Long id) {
        var mbUser = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessage.userNotFoundMessage(id)));
        var dto = mapper.map(mbUser);
        return dto;
    }

    public List<UserDTO> getAll() {
        var models = repository.findAll();
        var listDTO = models.stream()
                .map(mapper::map)
                .toList();
        return listDTO;
    }

    public UserDTO create(UserCreateDTO createDTO) {
        var model = mapper.map(createDTO);
        repository.save(model);
        var dto = mapper.map(model);
        return dto;
    }

    public UserDTO update(UserUpdateDTO updateDTO, Long maybeUserId) {
        var mbModel = repository.findById(maybeUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessage.userNotFoundMessage(maybeUserId)));
        mapper.update(updateDTO, mbModel);
        repository.save(mbModel);

        var dto = mapper.map(repository.save(mbModel));
        return dto;
    }

    public void delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ResourceNotFoundException(ExceptionMessage.userNotFoundMessage(id));
        }
    }

    public Long totalCount() {
        return repository.count();
    }

}
