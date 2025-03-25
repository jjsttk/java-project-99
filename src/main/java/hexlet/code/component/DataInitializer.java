package hexlet.code.component;


import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.label.LabelCreateDTO;
import hexlet.code.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public final class DataInitializer implements ApplicationRunner {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;
    private final TaskRepository taskRepository;
    private UserRepository userRepository;
    private UserMapper userMapper;
    private TaskStatusMapper taskStatusMapper;
    private TaskStatusRepository taskStatusRepository;
    private TaskMapper taskMapper;


    @Override
    public void run(ApplicationArguments args) {
        saveTestUser();
        saveDefaultTaskStatuses();
        saveDefaultLabels();
        saveTestTask();
    }

    private void saveTestTask() {
        var task = createTestTask();
        taskRepository.save(task);
    }

    private Task createTestTask() {
        var taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setAssigneeId(1L);

        var testStatus = new TaskStatusCreateDTO();
        testStatus.setName("Test");
        testStatus.setSlug("test");

        taskCreateDTO.setStatus(taskStatusRepository.save(taskStatusMapper.map(testStatus)).getSlug());
        taskCreateDTO.setTitle("test title");
        taskCreateDTO.setIndex(12);
        taskCreateDTO.setContent("test content");
        return taskMapper.map(taskCreateDTO);
    }

    private void saveTestUser() {
        var admin = createTestUser();
        userRepository.save(admin);
    }

    private User createTestUser() {
        var adminCreateDTO = new UserCreateDTO();
        adminCreateDTO.setEmail("hexlet@example.com");
        adminCreateDTO.setPassword("qwerty");
        var model = userMapper.map(adminCreateDTO);
        return model;
    }

    private List<TaskStatus> createDefaultTaskStatuses() {
        var listCreateDTO = new ArrayList<TaskStatusCreateDTO>();

        var draftStatus = new TaskStatusCreateDTO();
        draftStatus.setName("Draft");
        draftStatus.setSlug("draft");
        listCreateDTO.add(draftStatus);

        var toReviewStatus = new TaskStatusCreateDTO();
        toReviewStatus.setName("ToReview");
        toReviewStatus.setSlug("to_review");
        listCreateDTO.add(toReviewStatus);

        var toBeFixedStatus = new TaskStatusCreateDTO();
        toBeFixedStatus.setName("ToBeFixed");
        toBeFixedStatus.setSlug("to_be_fixed");
        listCreateDTO.add(toBeFixedStatus);

        var toPublishStatus = new TaskStatusCreateDTO();
        toPublishStatus.setName("ToPublish");
        toPublishStatus.setSlug("to_publish");
        listCreateDTO.add(toPublishStatus);

        var publishedStatus = new TaskStatusCreateDTO();
        publishedStatus.setName("Published");
        publishedStatus.setSlug("published");
        listCreateDTO.add(publishedStatus);

        return listCreateDTO.stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    private void saveDefaultTaskStatuses() {
        var models = createDefaultTaskStatuses();
        taskStatusRepository.saveAll(models);
    }

    private List<Label> createDefaultLabels() {
        var listCreateDTO = new ArrayList<LabelCreateDTO>();

        var featureLabel = new LabelCreateDTO();
        featureLabel.setName("feature");
        listCreateDTO.add(featureLabel);

        var bugLabel = new LabelCreateDTO();
        bugLabel.setName("bug");
        listCreateDTO.add(bugLabel);

        return listCreateDTO.stream()
                .map(labelMapper::map)
                .toList();

    }

    private void saveDefaultLabels() {
        var models = createDefaultLabels();
        labelRepository.saveAll(models);
    }
}
