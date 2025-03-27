package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.user.UserService;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testFullFieldsUserModel;
    private User testOnlyReqFieldsUserModel;
    private User testNonValidDataInFieldsUserModel;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        clearAllDataInDB();
        testFullFieldsUserModel = Instancio.of(modelGenerator.getFullFieldsUserModel()).create();
        testOnlyReqFieldsUserModel = Instancio.of(modelGenerator.getOnlyReqFieldsUserModel()).create();
        testNonValidDataInFieldsUserModel = Instancio.of(modelGenerator.getNonValidDataInFieldsUserModel()).create();

        if (testInfo.getDisplayName().equals("testUpdateWithoutRights()")) {
            var user = new User();
            user.setEmail("test@user.com");
            user.setPassword(passwordEncoder.encode("qwerty"));
            userRepository.save(user);
        }
    }

    @Test
    public void testResourceNotFoundException() {
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getById(9999L);
        });
        assertThat(exception.getMessage()).isEqualTo("User with id 9999 not found");
    }

    @Test
    public void testIndexWithAuthorization() throws Exception {
        var firstModel = testFullFieldsUserModel;
        firstModel.setPassword(passwordEncoder.encode(firstModel.getPassword()));
        userRepository.save(firstModel);

        var secondModel = userRepository.save(testOnlyReqFieldsUserModel);
        secondModel.setPassword(passwordEncoder.encode(secondModel.getPassword()));
        userRepository.save(secondModel);

        var result = mockMvc.perform(get("/api/users").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray().hasSize(2);

        var jsonIndexOfFirstTestedModel = "[0]";
        var jsonIndexOfSecondTestedModel = "[1]";
        assertThatJson(body).inPath(jsonIndexOfFirstTestedModel).and(
                v -> v.node("id").isEqualTo(testFullFieldsUserModel.getId()),
                v -> v.node("email").isEqualTo(testFullFieldsUserModel.getEmail()),
                v -> v.node("firstName").isEqualTo(testFullFieldsUserModel.getFirstName()),
                v -> v.node("lastName").isEqualTo(testFullFieldsUserModel.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );

        assertThatJson(body).inPath(jsonIndexOfSecondTestedModel).and(
                v -> v.node("id").isEqualTo(testOnlyReqFieldsUserModel.getId()),
                v -> v.node("email").isEqualTo(testOnlyReqFieldsUserModel.getEmail()),
                v -> v.node("createdAt").isNotNull()
        );

    }

    @Test
    public void testIndexWithoutAuthorization() throws Exception {
        var firstModel = testFullFieldsUserModel;
        firstModel.setPassword(passwordEncoder.encode(firstModel.getPassword()));
        userRepository.save(firstModel);
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShowWithFullFieldsUserModel() throws Exception {
        var model = testFullFieldsUserModel;
        model.setPassword(passwordEncoder.encode(model.getPassword()));
        userRepository.save(model);

        var request = get("/api/users/{id}", model.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isNotNull(),
                v -> v.node("email").isEqualTo(model.getEmail()),
                v -> v.node("firstName").isEqualTo(model.getFirstName()),
                v -> v.node("lastName").isEqualTo(model.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testShowWithOnlyReqFieldsUserModel() throws Exception {
        var model = testFullFieldsUserModel;
        model.setPassword(passwordEncoder.encode(model.getPassword()));
        userRepository.save(model);

        var request = get("/api/users/{id}", model.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("id").isNotNull(),
                v -> v.node("email").isEqualTo(model.getEmail()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        var model = testFullFieldsUserModel;
        model.setPassword(passwordEncoder.encode(model.getPassword()));
        userRepository.save(model);

        var request = get("/api/users/{id}", model.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testIndexWithoutAuth() throws Exception {
        var model = testFullFieldsUserModel;
        model.setPassword(passwordEncoder.encode(model.getPassword()));
        userRepository.save(model);
        var result = mockMvc.perform(get("/api/users/"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testCreateUserWithFullFieldsModel() throws Exception {
        var testUserModel = testFullFieldsUserModel;
        var createDTO = new UserCreateDTO();
        createDTO.setEmail(testUserModel.getEmail());
        createDTO.setFirstName(testUserModel.getFirstName());
        createDTO.setLastName(testUserModel.getLastName());
        createDTO.setPassword(testUserModel.getPassword());


        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        var mbUser = userRepository.findByEmail(createDTO.getEmail());
        assertThat(mbUser).isPresent();
        var userFromDB = mbUser.get();

        assertThat(userFromDB).isNotNull();
        assertThat(userFromDB.getFirstName()).isEqualTo(createDTO.getFirstName());
        assertThat(userFromDB.getLastName()).isEqualTo(createDTO.getLastName());
        assertThat(userFromDB.getEmail()).isEqualTo(createDTO.getEmail());
        assertThat(userFromDB.getPassword()).isNotEqualTo(createDTO.getPassword());

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isPresent(),
                v -> v.node("email").isEqualTo(createDTO.getEmail()),
                v -> v.node("firstName").isEqualTo(createDTO.getFirstName()),
                v -> v.node("lastName").isEqualTo(createDTO.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );

    }

    @Test
    public void testCreateUserWithOnlyReqFieldsModel() throws Exception {
        var testUser = testOnlyReqFieldsUserModel;

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        var mbUser = userRepository.findByEmail(testUser.getEmail());
        assertThat(mbUser).isPresent();
        var userFromDB = mbUser.get();

        assertThat(userFromDB.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(userFromDB.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(userFromDB.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(userFromDB.getPassword()).isNotEqualTo(testUser.getPassword());

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isPresent(),
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testCreateUserWithFullFilledCreateDTO() throws Exception {
        var model = testFullFieldsUserModel;

        var userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName(model.getFirstName());
        userCreateDTO.setEmail(model.getEmail());
        userCreateDTO.setLastName(model.getLastName());
        userCreateDTO.setPassword(model.getPassword());

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(userCreateDTO.getEmail()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(userCreateDTO.getFirstName());
        assertThat(user.getLastName()).isEqualTo(userCreateDTO.getLastName());
        assertThat(user.getEmail()).isEqualTo(userCreateDTO.getEmail());
        assertThat(user.getPassword()).isNotEqualTo(userCreateDTO.getPassword());
    }

    @Test
    @Transactional
    public void testCreateUserWithOnlyReqFilledCreateDTO() throws Exception {
        var model = testOnlyReqFieldsUserModel;
        var userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail(model.getEmail());
        userCreateDTO.setPassword(model.getPassword());

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(userCreateDTO.getEmail()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("");
        assertThat(user.getLastName()).isEqualTo("");
        assertThat(user.getEmail()).isEqualTo(userCreateDTO.getEmail());
        assertThat(user.getPassword()).isNotEqualTo(userCreateDTO.getPassword());
    }

    @Test
    @WithMockUser(username = "fullFields@model.com", roles = "USER")
    public void testUpdateUser() throws Exception {
        var modelBeforeUpdate = testFullFieldsUserModel;
        modelBeforeUpdate.setPassword(passwordEncoder.encode(modelBeforeUpdate.getPassword()));
        userRepository.save(modelBeforeUpdate);

        var modelBeforeUpdateId = modelBeforeUpdate.getId();

        var updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of("asd@mail.ru"));
        updateDTO.setFirstName(JsonNullable.of("TestName"));
        updateDTO.setLastName(JsonNullable.of("TestLastName"));
        updateDTO.setPassword(JsonNullable.of("qwerty"));

        var request = put("/api/users/{id}", modelBeforeUpdate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = response.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelBeforeUpdateId),
                v -> v.node("email").isEqualTo(updateDTO.getEmail()),
                v -> v.node("firstName").isEqualTo(updateDTO.getFirstName()),
                v -> v.node("lastName").isEqualTo(updateDTO.getLastName())
        );

        var updatedModelFromDB = userRepository.findById(modelBeforeUpdateId).get();

        assertThat(updatedModelFromDB.getLastName()).isEqualTo(updateDTO.getLastName().get());
        assertThat(updatedModelFromDB.getFirstName()).isEqualTo(updateDTO.getFirstName().get());
        assertThat(updatedModelFromDB.getEmail()).isEqualTo(updateDTO.getEmail().get());
        assertThat(passwordEncoder.matches(updateDTO.getPassword().get(), updatedModelFromDB.getPassword())).isTrue();
    }

    @Test
    @WithMockUser(username = "fullFields@model.com", roles = "USER")
    public void testPartialUpdateUser() throws Exception {
        var modelBeforeUpdate = testFullFieldsUserModel;
        modelBeforeUpdate.setPassword(passwordEncoder.encode(modelBeforeUpdate.getPassword()));
        userRepository.save(modelBeforeUpdate);

        var oldEmail = modelBeforeUpdate.getEmail();
        var oldFirstName = modelBeforeUpdate.getFirstName();
        var oldLastName = modelBeforeUpdate.getLastName();
        var modelBeforeUpdateId = modelBeforeUpdate.getId();

        var updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of("asdf@mail.ru"));
        updateDTO.setPassword(JsonNullable.of("qwerty"));

        var request = put("/api/users/{id}", modelBeforeUpdateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        var result = mockMvc.perform(request)
                .andExpect(status().isOk()).andReturn();

        var modelAfterUpdate = userRepository.findById(modelBeforeUpdate.getId()).get();
        var newPassword = modelAfterUpdate.getPassword();

        assertThat(passwordEncoder.matches("qwerty", newPassword)).isTrue();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelBeforeUpdateId),
                v -> v.node("email").isNotEqualTo(oldEmail),
                v -> v.node("firstName").isEqualTo(oldFirstName),
                v -> v.node("lastName").isEqualTo(oldLastName)
        );

        var updatedModelFromDB = userRepository.findById(modelBeforeUpdate.getId()).get();

        assertThat(updatedModelFromDB.getLastName()).isEqualTo(oldLastName);
        assertThat(updatedModelFromDB.getFirstName()).isEqualTo(oldFirstName);
        assertThat(updatedModelFromDB.getEmail()).isNotEqualTo(oldEmail);
    }

    @Test
    public void testCreateWithNonValidDataInRequestBody() throws Exception {
        var testUser = testNonValidDataInFieldsUserModel;

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "onlyReqFields@model.com", roles = "USER")
    public void testDeleteUserWithAuth() throws Exception {
        var testUser = testOnlyReqFieldsUserModel;
        testUser.setPassword(passwordEncoder.encode(testUser.getPassword()));
        userRepository.save(testUser);

        var id = testUser.getId();
        assertThat(userRepository.existsById(id)).isTrue();

        var request = delete("/api/users/{id}", id);
        var result = mockMvc.perform(request)
                        .andExpect(status().isNoContent());
        assertThat(userRepository.existsById(id)).isFalse();
    }

    @Test
    public void testDeleteUserWithoutAuth() throws Exception {
        var testUser = testOnlyReqFieldsUserModel;
        testUser.setPassword(passwordEncoder.encode(testUser.getPassword()));
        userRepository.save(testUser);

        var id = testUser.getId();
        assertThat(userRepository.existsById(id)).isTrue();

        var request = delete("/api/users/{id}", id);
        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
        assertThat(userRepository.existsById(id)).isTrue();
    }

    @Test
    @WithMockUser(username = "test@user.com", roles = "USER")
    public void testUpdateWithoutRights() throws Exception {
        var testedUser = testFullFieldsUserModel;
        testedUser.setPassword(passwordEncoder.encode(testedUser.getPassword()));
        userRepository.save(testedUser);

        var updateDTO = new UserUpdateDTO();
        var newPassword = "qwerty";
        updateDTO.setEmail(JsonNullable.of("asd@mail.ru"));
        updateDTO.setPassword(JsonNullable.of(newPassword));

        var request = put("/api/users/{id}", testedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        var result = mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    private void clearAllDataInDB() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();
    }
}
