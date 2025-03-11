package hexlet.code.app.component;

import hexlet.code.app.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Log4j2
public final class DataInitializer implements ApplicationRunner {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private TaskStatusMapper taskStatusMapper;
    private TaskStatusRepository taskStatusRepository;


    @Override
    public void run(ApplicationArguments args) {
        saveAdmin();
        saveTaskStatusExamples();

    }

    private void saveAdmin() {
        var admin = createAdmin();
        userRepository.save(admin);
    }

    private User createAdmin() {
        var adminCreateDTO = new UserCreateDTO();
        adminCreateDTO.setEmail("hexlet@example.com");
        adminCreateDTO.setPassword("qwerty");
        var model = userMapper.mapToEntity(adminCreateDTO);
        return model;
    }

    private List<TaskStatus> createTaskStatusExamples() {
        var listDTO = new ArrayList<TaskStatusCreateDTO>();

        var draftStatus = new TaskStatusCreateDTO();
        draftStatus.setName("example with slug \"draft\"");
        draftStatus.setSlug("draft");
        listDTO.add(draftStatus);

        var toReviewStatus = new TaskStatusCreateDTO();
        toReviewStatus.setName("example with slug \"to_review\"");
        toReviewStatus.setSlug("to_review");
        listDTO.add(toReviewStatus);

        var toBeFixedStatus = new TaskStatusCreateDTO();
        toBeFixedStatus.setName("example with slug \"to_be_fixed\"");
        toBeFixedStatus.setSlug("to_be_fixed");
        listDTO.add(toBeFixedStatus);

        var toPublishStatus = new TaskStatusCreateDTO();
        toPublishStatus.setName("example with slug \"to_publish\"");
        toPublishStatus.setSlug("to_publish");
        listDTO.add(toPublishStatus);

        var publishedStatus = new TaskStatusCreateDTO();
        publishedStatus.setName("example with slug \"published\"");
        publishedStatus.setSlug("published");
        listDTO.add(publishedStatus);

        return listDTO.stream()
                .map(taskStatusMapper::mapToEntity)
                .toList();
    }

    private void saveTaskStatusExamples() {
        var models = createTaskStatusExamples();
        taskStatusRepository.saveAll(models);
    }
}
