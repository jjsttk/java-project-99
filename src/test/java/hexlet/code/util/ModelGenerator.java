package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.model.Task;
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
    private Model<Task> taskWithToReviewTaskStatus;
    private Model<Task> taskWithDraftTaskStatus;

    private Model<List<Label>> labels;

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

        taskWithToReviewTaskStatus = buildTaskWithToReviewTaskStatus();
        taskWithDraftTaskStatus = buildTaskWithDraftTaskStatus();

        labels = buildTestLabels(5);

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

    private Model<Task> buildTaskWithToReviewTaskStatus() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getAssignee), this::buildOnlyReqFieldsUserModel)
                .supply(Select.field(Task::getTaskStatus), this::buildToReviewTestTaskStatus)
                .toModel();
    }

    private Model<Task> buildTaskWithDraftTaskStatus() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getAssignee), this::buildOnlyReqFieldsUserModel)
                .supply(Select.field(Task::getTaskStatus), this::buildDraftTestTaskStatus)
                .toModel();
    }

    private Model<List<Label>> buildTestLabels(int count) {
        return Instancio.ofList(Label.class)
                .size(count)
                .ignore(Select.field(Label::getId))
                .supply(Select.field(Label::getName), () -> faker.gameOfThrones().character())
                .toModel();
    }

    private String generatePassword(int minLength, int maxLength) {
        int length = faker.number().numberBetween(minLength, maxLength);
        return faker.lorem().characters(length);
    }

}
