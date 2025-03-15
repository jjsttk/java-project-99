package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task.status.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.service.task.status.TaskStatusService;
import hexlet.code.app.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class TaskStatusControllerTest {
    private List<TaskStatus> testModels;

    @Autowired
    private TaskStatusRepository repository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private TaskStatusService service;


    @BeforeEach
    public void setup() {
        testModels = Instancio.of(modelGenerator.getValidTaskStatuses()).create();
    }

    @Test
    public void modelGenerateTest() {
        assertThat(testModels.size()).isEqualTo(ModelGenerator.TASK_STATUS_MODELS_TO_GENERATE);
    }

    @Test
    @Transactional
    public void modelsSaveTest() {
        var countBeforeSave = repository.count();
        repository.saveAll(testModels);
        var countAfterSave = repository.count();
        var modelsSize = testModels.size();

        assertThat(countAfterSave - countBeforeSave).isEqualTo(modelsSize);
    }

    @Test
    public void testResourceNotFoundException() {
        var id = 9999L;
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.getById(id));
        assertThat(exception.getMessage()).isEqualTo("TaskStatus with id " + id + " not found");


        var slug = "unknownSlug";
        Exception exception1 = assertThrows(ResourceNotFoundException.class, () -> service.getBySlug(slug));
        assertThat(exception1.getMessage()).isEqualTo("TaskStatus with slug " + slug + " not found");
    }

    @Test
    @Transactional
    public void testIndexWithAuthorization() throws Exception {
        var countBeforeAddTestModels = repository.count();

        repository.saveAll(testModels);

        var countAfterAddTestModels = (int) repository.count();

        var result = mockMvc.perform(get("/api/task_statuses")
                        .with(jwt()))
                .andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        var firstTestedModel = testModels.get(0);
        var secondTestedModel = testModels.get(1);

        var jsonIndexOfFirstTestedModel = String.format("[%d]", countBeforeAddTestModels);
        var jsonIndexOfSecondTestedModel = String.format("[%d]", countBeforeAddTestModels + 1);

        assertThat(countBeforeAddTestModels).isNotEqualTo(countAfterAddTestModels);
        assertThat(countAfterAddTestModels - testModels.size()).isEqualTo(countBeforeAddTestModels);
        assertThatJson(body).isArray().hasSize(countAfterAddTestModels);
        assertThatJson(body).inPath(jsonIndexOfFirstTestedModel).and(
                v -> v.node("id").isEqualTo(firstTestedModel.getId()),
                v -> v.node("name").isEqualTo(firstTestedModel.getName()),
                v -> v.node("slug").isEqualTo(firstTestedModel.getSlug()),
                v -> v.node("createdAt").isNotNull()
        );

        assertThatJson(body).inPath(jsonIndexOfSecondTestedModel).and(
                v -> v.node("id").isEqualTo(secondTestedModel.getId()),
                v -> v.node("name").isEqualTo(secondTestedModel.getName()),
                v -> v.node("slug").isEqualTo(secondTestedModel.getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }



    @Test
    public void testIndexWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testShowWithAuthorization() throws Exception {
        var savedModelsInDB = repository.saveAll(testModels);
        var firstTestedModel = savedModelsInDB.getFirst();

        var resultBody = mockMvc.perform(get("/api/task_statuses/{id}", firstTestedModel.getId())
                .with(jwt()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(resultBody).and(
                v -> v.node("id").isEqualTo(firstTestedModel.getId()),
                v -> v.node("name").isEqualTo(firstTestedModel.getName()),
                v -> v.node("slug").isEqualTo(firstTestedModel.getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testShowWithOutAuthorization() throws Exception {
        var savedModelsInDB = repository.saveAll(testModels);
        var firstTestedModel = savedModelsInDB.getFirst();
        mockMvc.perform(get("/api/task_statuses/{id}", firstTestedModel.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testCreateNewTaskStatusFromModelWithAuthorization() throws Exception {
        var model = testModels.getFirst();

        var request = post("/api/task_statuses")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(model));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        var mbModelFromDB = repository.findBySlug(model.getSlug());
        assertThat(mbModelFromDB).isPresent();

        var modelFromDB = mbModelFromDB.get();
        assertThat(model.getName()).isEqualTo(modelFromDB.getName());
        assertThat(model.getSlug()).isEqualTo(modelFromDB.getSlug());

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelFromDB.getId()),
                v -> v.node("name").isEqualTo(modelFromDB.getName()),
                v -> v.node("slug").isEqualTo(modelFromDB.getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testCreateNewTaskStatusFromModelWithOutAuthorization() throws Exception {
        var model = testModels.getFirst();

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(model));

        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testCreateNewTaskStatusFromCreateDTO() throws Exception {
        var model = testModels.getFirst();

        var createDTO = new TaskStatusCreateDTO();
        createDTO.setName(model.getName());
        createDTO.setSlug(model.getSlug());

        var request = post("/api/task_statuses")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        var mbModelFromDB = repository.findBySlug(createDTO.getSlug());
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThat(createDTO.getName()).isEqualTo(modelFromDB.getName());
        assertThat(createDTO.getSlug()).isEqualTo(modelFromDB.getSlug());

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelFromDB.getId()),
                v -> v.node("name").isEqualTo(modelFromDB.getName()),
                v -> v.node("slug").isEqualTo(modelFromDB.getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testUpdateTaskStatusUsingAllFieldsInUpdateDTO() throws Exception {
        var model = repository.save(testModels.getFirst());

        var oldId = model.getId();
        var oldName = model.getName();
        var oldSlug = model.getSlug();

        var updateDTO = new TaskStatusUpdateDTO();
        var newName = "Updated name";
        var newSlug = "updated_slug";
        updateDTO.setName(JsonNullable.of("Updated name"));
        updateDTO.setSlug(JsonNullable.of("updated_slug"));

        var request = put("/api/task_statuses/{id}", oldId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var mbModelFromDB = repository.findBySlug(newSlug);
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThat(oldName).isNotEqualTo(modelFromDB.getName());
        assertThat(oldSlug).isNotEqualTo(modelFromDB.getSlug());
        assertThat(modelFromDB.getName()).isEqualTo(newName);
        assertThat(modelFromDB.getSlug()).isEqualTo(newSlug);

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(model.getId()),
                v -> v.node("name").isEqualTo(newName),
                v -> v.node("slug").isEqualTo(newSlug),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testPartialUpdateTaskStatusUsingOneFieldInUpdateDTO() throws Exception {
        var model = repository.save(testModels.getFirst());

        var oldId = model.getId();
        var oldName = model.getName();
        var oldSlug = model.getSlug();

        var updateDTO = new TaskStatusUpdateDTO();
        var newSlug = "updated_slug";
        updateDTO.setSlug(JsonNullable.of("updated_slug"));

        var request = put("/api/task_statuses/{id}", oldId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var mbModelFromDB = repository.findBySlug(newSlug);
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThat(oldSlug).isNotEqualTo(modelFromDB.getSlug());
        assertThat(modelFromDB.getName()).isEqualTo(oldName);
        assertThat(modelFromDB.getSlug()).isEqualTo(newSlug);

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(model.getId()),
                v -> v.node("name").isEqualTo(model.getName()),
                v -> v.node("slug").isEqualTo(newSlug),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testUpdateTaskStatusWithoutAuth() throws Exception {
        var model = repository.save(testModels.getFirst());

        var updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(JsonNullable.of("Updated name"));
        updateDTO.setSlug(JsonNullable.of("updated_slug"));

        var request = put("/api/task_statuses/{id}", model.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteTaskStatusWithAuth() throws Exception {
        var model = repository.save(testModels.getFirst());
        var request = delete("/api/task_statuses/{id}",
                model.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());

        assertThat(repository.findBySlug(model.getSlug())).isEmpty();
        assertThat(repository.findById(model.getId())).isEmpty();
        assertThrows(ResourceNotFoundException.class, () -> service.getBySlug(model.getSlug()));
        assertThrows(ResourceNotFoundException.class, () -> service.getById(model.getId()));
    }

    @Test
    public void testDeleteTaskStatusWithoutAuth() throws Exception {
        var model = repository.save(testModels.getFirst());
        var request = delete("/api/task_statuses/{id}",
                model.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());

        assertThat(repository.findBySlug(model.getSlug())).isNotEmpty();
        assertThat(repository.findById(model.getId())).isNotEmpty();
    }

}
