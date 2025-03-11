package hexlet.code.app.util;

import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
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
    private Model<User> testAdmin;

    public static final int TASK_STATUS_MODELS_COUNT = 5;

    @Autowired
    @Getter(AccessLevel.NONE)
    private Faker faker;

    @PostConstruct
    private void init() {
        testAdmin = buildTestAdmin();

        fullFieldsUserModel = buildFullFieldsUserModel();
        onlyReqFieldsUserModel = buildOnlyReqFieldsUserModel();
        nonValidDataInFieldsUserModel = buildNonValidDataInFieldsUserModel();
        validTaskStatuses = buildValidTaskStatuses();
    }

    private Model<User> buildTestAdmin() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> "admin@test.com")
                .supply(Select.field(User::getPassword), () -> "qwerty")
                .toModel();
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
                .size(TASK_STATUS_MODELS_COUNT)
                .ignore(Select.field(TaskStatus::getId))
                .toModel();
    }

    private String generatePassword(int minLength, int maxLength) {
        int length = faker.number().numberBetween(minLength, maxLength);
        return faker.lorem().characters(length);
    }
}
