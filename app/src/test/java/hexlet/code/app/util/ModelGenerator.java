package hexlet.code.app.util;

import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.model.Task;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class ModelGenerator {
    private Model<User> fullFieldsUserModel;
    private Model<User> onlyReqFieldsUserModel;
    private Model<User> nonValidDataInFieldsUserModel;
    private Model<List<TaskStatus>> validTaskStatuses;

    private List<Task> tasks;
    private Model<TaskStatus> draftTestTaskStatus;
    private Model<TaskStatus> toReviewTestTaskStatus;


    private Model<User> testAdmin;




    public static final int TASK_STATUS_MODELS_TO_GENERATE = 5;

    @Autowired
    @Getter(AccessLevel.NONE)
    private Faker faker;

    @PostConstruct
    private void init() {
        fullFieldsUserModel = buildFullFieldsUserModel();
        onlyReqFieldsUserModel = buildOnlyReqFieldsUserModel();
        nonValidDataInFieldsUserModel = buildNonValidDataInFieldsUserModel();
        validTaskStatuses = buildValidTaskStatuses();

        toReviewTestTaskStatus = buildToReviewTestTaskStatus();
        draftTestTaskStatus = buildDraftTestTaskStatus();
        tasks = buildTasks();
    }

    private Model<User> buildFullFieldsUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> "fullFields@model.com")
                .supply(Select.field(User::getPassword), () -> generatePassword(3, 100))
                .toModel();
    }

    private Model<User> buildOnlyReqFieldsUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> "onlyReqFields@model.com")
                .supply(Select.field(User::getPassword), () -> generatePassword(3, 100))
                .toModel();
    }

    private Model<User> buildNonValidDataInFieldsUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> "asd.com")
                .supply(Select.field(User::getPassword), () -> "qw")
                .toModel();
    }

    private Model<List<TaskStatus>> buildValidTaskStatuses() {
        return Instancio.ofList(TaskStatus.class)
                .size(TASK_STATUS_MODELS_TO_GENERATE)
                .ignore(Select.field(TaskStatus::getId))
                .toModel();
    }

    private Model<TaskStatus> buildDraftTestTaskStatus() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.name().title())
                .supply(Select.field(TaskStatus::getSlug), () -> "draft_test")
                .toModel();
    }

    private Model<TaskStatus> buildToReviewTestTaskStatus() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.name().title())
                .supply(Select.field(TaskStatus::getSlug), () -> "to_review_test")
                .toModel();
    }

    private List<Task> buildTasks() {
        var taskWithDraftTaskStatus = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getAssignee), () -> Instancio.create(onlyReqFieldsUserModel))
                .supply(Select.field(Task::getTaskStatus), () -> Instancio.create(draftTestTaskStatus))
                .create();

        var taskWithToReviewTaskStatus = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getAssignee), () -> Instancio.create(onlyReqFieldsUserModel))
                .supply(Select.field(Task::getTaskStatus), () -> Instancio.create(toReviewTestTaskStatus))
                .create();

        return List.of(taskWithDraftTaskStatus, taskWithToReviewTaskStatus);

    }

    private String generatePassword(int minLength, int maxLength) {
        int length = faker.number().numberBetween(minLength, maxLength);
        return faker.lorem().characters(length);
    }

}
