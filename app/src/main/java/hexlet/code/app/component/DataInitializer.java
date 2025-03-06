package hexlet.code.app.component;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public final class DataInitializer implements ApplicationRunner {

    private UserRepository userRepository;
    private UserMapper userMapper;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        var adminCreateDTO = new UserCreateDTO();
        adminCreateDTO.setEmail("hexlet@example.com");
        adminCreateDTO.setPassword("qwerty");
        var admin = userMapper.map(adminCreateDTO);
        userRepository.save(admin);
    }
}
