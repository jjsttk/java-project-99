package hexlet.code.app.util;

import hexlet.code.app.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ModelGenerator {
    private Model<User> fullFieldsUserModel;
    private Model<User> onlyReqFieldsUserModel;
    private Model<User> nonValidDataInFieldsUserModel;

    @Autowired
    private Faker faker;

    @PostConstruct
    private void init() {
        fullFieldsUserModel = buildFullFieldsUserModel();
        onlyReqFieldsUserModel = buildOnlyReqFieldsUserModel();
        nonValidDataInFieldsUserModel = buildNonValidDataInFieldsUserModel();
    }

    private Model<User> buildFullFieldsUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> "fullFields@model.com")
                .supply(Select.field(User::getPassword), () -> faker.internet().password(3, 100))
                .toModel();
    }

    private Model<User> buildOnlyReqFieldsUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> "onlyReqFields@model.com")
                .supply(Select.field(User::getPassword), () -> faker.internet().password(3, 100))
                .toModel();
    }

    private Model<User> buildNonValidDataInFieldsUserModel() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> "asd.com")
                .supply(Select.field(User::getPassword), () -> "qw")
                .toModel();
    }
}
