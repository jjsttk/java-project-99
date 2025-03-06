package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.UserService;
import hexlet.code.app.util.ModelGenerator;
import lombok.extern.log4j.Log4j2;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
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
    private ModelGenerator modelGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testFullFieldsUserModel;
    private User testOnlyReqFieldsUserModel;
    private User testNonValidDataInFieldsUserModel;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        testFullFieldsUserModel = Instancio.of(modelGenerator.getFullFieldsUserModel()).create();
        testOnlyReqFieldsUserModel = Instancio.of(modelGenerator.getOnlyReqFieldsUserModel()).create();
        testNonValidDataInFieldsUserModel = Instancio.of(modelGenerator.getNonValidDataInFieldsUserModel()).create();
    }

    @Test
    @Transactional
    public void testIndexWithAuthorization() throws Exception {
        var firstModel = userRepository.save(testFullFieldsUserModel);
        var secondModel = userRepository.save(testOnlyReqFieldsUserModel);

        var result = mockMvc.perform(get("/api/users").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        // Expected 3 not 2, coz of we create +1 user (Admin) in DataInitializer.class"
        assertThatJson(body).isArray().hasSize(3);
        assertThatJson(body).inPath("[1]").and(
                v -> v.node("id").isEqualTo(testFullFieldsUserModel.getId()),
                v -> v.node("email").isEqualTo(testFullFieldsUserModel.getEmail()),
                v -> v.node("firstName").isEqualTo(testFullFieldsUserModel.getFirstName()),
                v -> v.node("lastName").isEqualTo(testFullFieldsUserModel.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );

        assertThatJson(body).inPath("[2]").and(
                v -> v.node("id").isEqualTo(testOnlyReqFieldsUserModel.getId()),
                v -> v.node("email").isEqualTo(testOnlyReqFieldsUserModel.getEmail()),
                v -> v.node("createdAt").isNotNull()
        );

    }

    @Test
    @Transactional
    public void testIndexWithoutAuthorization() throws Exception {
        var firstModel = userRepository.save(testFullFieldsUserModel);

        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testShowWithFullFieldsUserModel() throws Exception {
        var model = userRepository.save(testFullFieldsUserModel);

        var request = get("/api/users/{id}", model.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        var testUserFromDB = userRepository.findByEmail(model.getEmail()).get();

        assertThatJson(body).and(
                v -> v.node("id").isNotNull(),
                v -> v.node("email").isEqualTo(testUserFromDB.getEmail()),
                v -> v.node("firstName").isEqualTo(testUserFromDB.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUserFromDB.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testShowWithOnlyReqFieldsUserModel() throws Exception {
        var model = userRepository.save(testOnlyReqFieldsUserModel);

        var request = get("/api/users/{id}", model.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        var testUserFromDB = userRepository.findByEmail(model.getEmail()).get();
        assertThatJson(body).and(
                v -> v.node("id").isNotNull(),
                v -> v.node("email").isEqualTo(testUserFromDB.getEmail()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testShowWithoutAuth() throws Exception {

        userRepository.save(testFullFieldsUserModel);

        var request = get("/api/users/{id}", testFullFieldsUserModel.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Transactional
    public void testIndexWithoutAuth() throws Exception {
        userRepository.save(testFullFieldsUserModel);
        var result = mockMvc.perform(get("/api/users/"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @Transactional
    public void testCreateUserWithFullFieldsModel() throws Exception {
        var testUser = testFullFieldsUserModel;

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(testUser.getEmail()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(user.getPassword()).isNotEqualTo(testUser.getPassword());
    }

    @Test
    @Transactional
    public void testCreateUserWithOnlyReqFieldsModel() throws Exception {
        var testUser = testOnlyReqFieldsUserModel;

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(testUser.getEmail()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(user.getPassword()).isNotEqualTo(testUser.getPassword());
    }

    @Test
    @Transactional
    public void testCreateUserWithCreateDTO() throws Exception {
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
    @WithMockUser(username = "fullFields@model.com", roles = "USER")
    public void testUpdateUser() throws Exception {
        var modelBeforeUpdate = userRepository.save(testFullFieldsUserModel);
        var oldEmail = modelBeforeUpdate.getEmail();
        var oldPassword = modelBeforeUpdate.getPassword();
        var oldFirstName = modelBeforeUpdate.getFirstName();
        var oldLastName = modelBeforeUpdate.getLastName();
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

        var updatedModelFromDB = userRepository.findById(modelBeforeUpdate.getId()).get();

        assertThat(updatedModelFromDB.getLastName()).isNotEqualTo(oldLastName);
        assertThat(updatedModelFromDB.getFirstName()).isNotEqualTo(oldFirstName);
        assertThat(updatedModelFromDB.getEmail()).isNotEqualTo(oldEmail);
        assertThat(updatedModelFromDB.getPassword()).isNotEqualTo(oldPassword);
        assertThat(passwordEncoder.matches("qwerty", updatedModelFromDB.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = "fullFields@model.com", roles = "USER")
    public void testPartialUpdateUser() throws Exception {
        var modelBeforeUpdate = userRepository.save(testFullFieldsUserModel);
        var oldEmail = modelBeforeUpdate.getEmail();
        var oldPassword = modelBeforeUpdate.getPassword();
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

        assertThat(oldPassword).isNotEqualTo(newPassword);
        assertThat(passwordEncoder.matches("qwerty", modelAfterUpdate.getPassword())).isTrue();

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
        assertThat(updatedModelFromDB.getPassword()).isNotEqualTo(oldPassword);
    }

    @Test
    @Transactional
    public void testCreateWithNonValidDataInRequestBody() throws Exception {
        var testUser = testNonValidDataInFieldsUserModel;

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(testUser));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(username = "onlyReqFields@model.com", roles = "USER")
    public void testDeleteUserWithAuth() throws Exception {
        var testUser = userRepository.save(testOnlyReqFieldsUserModel);
        var id = testUser.getId();
        assertThat(userRepository.existsById(id)).isTrue();

        var request = delete("/api/users/{id}", id);
        var result = mockMvc.perform(request)
                        .andExpect(status().isNoContent());
        assertThat(userRepository.existsById(id)).isFalse();
    }

    @Test
    @Transactional
    public void testDeleteUserWithoutAuth() throws Exception {
        var testUser = userRepository.save(testOnlyReqFieldsUserModel);
        var id = testUser.getId();
        assertThat(userRepository.existsById(id)).isTrue();

        var request = delete("/api/users/{id}", id);
        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
        assertThat(userRepository.existsById(id)).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(username = "hexlet@example.com", roles = "USER")
    public void testUpdateWithoutRights() throws Exception {
        var testedModel = userRepository.save(testFullFieldsUserModel);

        var updateDTO = new UserUpdateDTO();
        var newPassword = "qwerty";
        updateDTO.setEmail(JsonNullable.of("asd@mail.ru"));
        updateDTO.setPassword(JsonNullable.of(newPassword));

        var request = put("/api/users/{id}", testedModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        var result = mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }
}
