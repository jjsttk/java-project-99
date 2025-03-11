package hexlet.code.app.service.user;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public final class UserService extends BaseService<User, UserDTO,
        UserCreateDTO, UserUpdateDTO> {

    public UserService(UserRepository repository, UserMapper mapper) {
        super(repository, mapper, User.class);
    }
}
