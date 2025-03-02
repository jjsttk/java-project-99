package hexlet.code.app.service;


import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.exception.ExceptionMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDTO getUserById(Long id) {
        var mbUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessage.userNotFoundMessage(id)));
        var userDTO = userMapper.map(mbUser);
        return userDTO;
    }

    public List<UserDTO> getEntities() {
        var users = userRepository.findAll();
        var usersDTO = users.stream()
                .map(userMapper::map)
                .toList();
        return usersDTO;
    }

    public UserDTO create(UserCreateDTO createDTO) {
        var user = userMapper.map(createDTO);
        userRepository.save(user);
        var userDTO = userMapper.map(user);
        return userDTO;
    }

    public UserDTO update(UserUpdateDTO updateDTO, Long maybeUserId) {
        var mbUser = userRepository.findById(maybeUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessage.userNotFoundMessage(maybeUserId)));
        userMapper.update(updateDTO, mbUser);
        var userDTO = userMapper.map(userRepository.save(mbUser));
        return userDTO;
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

}
