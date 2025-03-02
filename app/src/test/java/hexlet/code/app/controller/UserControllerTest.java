package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.mapper.UserMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper mapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testFullFieldsUserModel;
    private User testOnlyReqFieldsUserModel;
    private User testNonValidDataInFieldsUserModel;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testFullFieldsUserModel = Instancio.of(modelGenerator.getFullFieldsUserModel()).create();
        testOnlyReqFieldsUserModel = Instancio.of(modelGenerator.getOnlyReqFieldsUserModel()).create();
        testNonValidDataInFieldsUserModel = Instancio.of(modelGenerator.getNonValidDataInFieldsUserModel()).create();

        userRepository.deleteAll();
    }

    @Test
    public void testIndexWithAuthorization() throws Exception {
        var firstModel = userRepository.save(testFullFieldsUserModel);
        var secondModel = userRepository.save(testOnlyReqFieldsUserModel);

        var result = mockMvc.perform(get("/api/users").with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray().hasSize(2);
        assertThatJson(body).inPath("[0]").and(
                v -> v.node("id").isEqualTo(testFullFieldsUserModel.getId()),
                v -> v.node("email").isEqualTo(testFullFieldsUserModel.getEmail()),
                v -> v.node("firstName").isEqualTo(testFullFieldsUserModel.getFirstName()),
                v -> v.node("lastName").isEqualTo(testFullFieldsUserModel.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );

        assertThatJson(body).inPath("[1]").and(
                v -> v.node("id").isEqualTo(testOnlyReqFieldsUserModel.getId()),
                v -> v.node("email").isEqualTo(testOnlyReqFieldsUserModel.getEmail()),
                v -> v.node("createdAt").isNotNull()
        );

    }

    @Test
    public void testIndexWithoutAuthorization() throws Exception {
        var firstModel = userRepository.save(testFullFieldsUserModel);

        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
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
                v -> v.node("firstName").isEqualTo(testUserFromDB.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUserFromDB.getLastName()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testShowWithoutAuth() throws Exception {

        userRepository.save(testFullFieldsUserModel);

        var request = get("/api/users/{id}", testFullFieldsUserModel.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testIndexWithoutAuth() throws Exception {
        userRepository.save(testFullFieldsUserModel);
        var result = mockMvc.perform(get("/api/users/"))
                .andExpect(status().isUnauthorized());

    }

    @Test
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
    public void testUpdateUser() throws Exception {
        var modelBeforeUpdate = userRepository.save(testFullFieldsUserModel);

        var updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of("asd@mail.ru"));
        updateDTO.setFirstName(JsonNullable.of("TestName"));
        updateDTO.setLastName(JsonNullable.of("TestLastName"));
        var newPassword = "qwerty";
        updateDTO.setPassword(JsonNullable.of(newPassword));

        var request = put("/api/users/{id}", modelBeforeUpdate.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = response.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelBeforeUpdate.getId()),
                v -> v.node("email").isEqualTo(updateDTO.getEmail()),
                v -> v.node("firstName").isEqualTo(updateDTO.getFirstName()),
                v -> v.node("lastName").isEqualTo(updateDTO.getLastName())
        );

        var updatedModelFromDB = userRepository.findById(modelBeforeUpdate.getId()).get();

        assertThat(updatedModelFromDB.getLastName()).isNotEqualTo(modelBeforeUpdate.getLastName());
        assertThat(updatedModelFromDB.getFirstName()).isNotEqualTo(modelBeforeUpdate.getFirstName());
        assertThat(updatedModelFromDB.getEmail()).isNotEqualTo(modelBeforeUpdate.getEmail());
        assertThat(updatedModelFromDB.getPassword()).isNotEqualTo(modelBeforeUpdate.getPassword());
        assertThat(passwordEncoder.matches(newPassword, updatedModelFromDB.getPassword())).isTrue();
    }

    @Test
    public void testPartialUpdateUser() throws Exception {
        var modelBeforeUpdate = userRepository.save(testFullFieldsUserModel);

        var updateDTO = new UserUpdateDTO();
        var newPassword = "qwerty";
        updateDTO.setEmail(JsonNullable.of("asd@mail.ru"));
        updateDTO.setPassword(JsonNullable.of(newPassword));

        var request = put("/api/users/{id}", modelBeforeUpdate.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        var result = mockMvc.perform(request)
                .andExpect(status().isOk()).andReturn();

        var modelAfterUpdate = userRepository.findById(modelBeforeUpdate.getId()).get();
        assertThat(modelBeforeUpdate.getPassword()).isNotEqualTo(modelAfterUpdate.getPassword());
        assertThat(passwordEncoder.matches(newPassword, modelAfterUpdate.getPassword())).isTrue();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelBeforeUpdate.getId()),
                v -> v.node("email").isNotEqualTo(modelBeforeUpdate.getEmail()),
                v -> v.node("firstName").isEqualTo(modelBeforeUpdate.getFirstName()),
                v -> v.node("lastName").isEqualTo(modelBeforeUpdate.getLastName())
        );

        var updatedModelFromDB = userRepository.findById(modelBeforeUpdate.getId()).get();

        assertThat(updatedModelFromDB.getLastName()).isEqualTo(modelBeforeUpdate.getLastName());
        assertThat(updatedModelFromDB.getFirstName()).isEqualTo(modelBeforeUpdate.getFirstName());
        assertThat(updatedModelFromDB.getEmail()).isNotEqualTo(modelBeforeUpdate.getEmail());
        assertThat(updatedModelFromDB.getPassword()).isNotEqualTo(modelBeforeUpdate.getPassword());
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
    public void testDeleteUserWithAuth() throws Exception {
        var testUser = userRepository.save(testOnlyReqFieldsUserModel);
        var id = testUser.getId();
        assertThat(userRepository.existsById(id)).isTrue();

        var request = delete("/api/users/{id}", id).with(jwt());
        var result = mockMvc.perform(request)
                        .andExpect(status().isNoContent());
        assertThat(userRepository.existsById(id)).isFalse();
    }

    @Test
    public void testDeleteUserWithoutAuth() throws Exception {
        var testUser = userRepository.save(testOnlyReqFieldsUserModel);
        var id = testUser.getId();
        assertThat(userRepository.existsById(id)).isTrue();

        var request = delete("/api/users/{id}", id);
        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
        assertThat(userRepository.existsById(id)).isTrue();
    }
}
