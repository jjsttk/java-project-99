package hexlet.code.app.service.user;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public final class UserService extends BaseService<User, UserDTO,
        UserCreateDTO, UserUpdateDTO> {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public UserService(UserRepository repository, UserMapper mapper, TaskRepository taskRep) {
        super(repository, mapper, User.class);
        this.taskRepository = taskRep;
        this.userRepository = repository;


    }

    @Override
    public void delete(Long id) {
        var hasTasks = taskRepository.existsByAssigneeId(id);
        if (hasTasks) {
            throw new IllegalStateException("Cannot delete user with id = " + id
                    + " ,because user was assigned to at least one task.");
        }
        userRepository.deleteById(id);
    }
}
