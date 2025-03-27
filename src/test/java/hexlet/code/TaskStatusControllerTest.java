package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.dto.task.status.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.task.status.TaskStatusService;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;

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
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        testModels = Instancio.of(modelGenerator.getValidTaskStatuses()).create();
    }

    @Test
    public void modelGenerateTest() {
        assertThat(testModels.size()).isEqualTo(ModelGenerator.TASK_STATUS_MODELS_TO_GENERATE);
    }

    @Test
    public void modelsSaveTest() {
        taskStatusRepository.saveAll(testModels);
        var countAfterSave = taskStatusRepository.count();
        assertThat(countAfterSave).isEqualTo(testModels.size());
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
    public void testIndexWithAuthorization() throws Exception {
        taskStatusRepository.saveAll(testModels);

        var result = mockMvc.perform(get("/api/task_statuses")
                        .with(jwt()))
                .andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        var firstTestedModel = testModels.get(0);
        var secondTestedModel = testModels.get(1);

        var jsonIndexOfFirstTestedModel = "[0]";
        var jsonIndexOfSecondTestedModel = "[1]";

        assertThatJson(body).isArray().hasSize(testModels.size());
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
    public void testShowWithAuthorization() throws Exception {
        var savedModelsInDB = taskStatusRepository.saveAll(testModels);
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
    public void testShowWithOutAuthorization() throws Exception {
        var savedModelsInDB = taskStatusRepository.saveAll(testModels);
        var firstTestedModel = savedModelsInDB.getFirst();
        mockMvc.perform(get("/api/task_statuses/{id}", firstTestedModel.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateNewTaskStatus() throws Exception {
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

        var mbModelFromDB = taskStatusRepository.findBySlug(createDTO.getSlug());
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThat(createDTO.getName()).isEqualTo(modelFromDB.getName());
        assertThat(createDTO.getSlug()).isEqualTo(modelFromDB.getSlug());

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isPresent(),
                v -> v.node("name").isEqualTo(createDTO.getName()),
                v -> v.node("slug").isEqualTo(createDTO.getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testUpdateTaskStatusUsingAllFieldsInUpdateDTO() throws Exception {
        var model = taskStatusRepository.save(testModels.getFirst());

        var updateDTO = new TaskStatusUpdateDTO();
        var newName = "Updated name";
        var newSlug = "updated_slug";
        updateDTO.setName(JsonNullable.of(newName));
        updateDTO.setSlug(JsonNullable.of(newSlug));

        var request = put("/api/task_statuses/{id}", model.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var mbModelFromDB = taskStatusRepository.findBySlug(newSlug);
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThat(modelFromDB.getName()).isEqualTo(updateDTO.getName().get());
        assertThat(modelFromDB.getSlug()).isEqualTo(updateDTO.getSlug().get());

        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(model.getId()),
                v -> v.node("name").isEqualTo(updateDTO.getName().get()),
                v -> v.node("slug").isEqualTo(updateDTO.getSlug().get()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testPartialUpdateTaskStatusUsingOneFieldInUpdateDTO() throws Exception {
        var model = taskStatusRepository.save(testModels.getFirst());
        var oldName = model.getName();

        var updateDTO = new TaskStatusUpdateDTO();
        var newSlug = "updated_slug";
        updateDTO.setSlug(JsonNullable.of("updated_slug"));

        var request = put("/api/task_statuses/{id}", model.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var mbModelFromDB = taskStatusRepository.findBySlug(newSlug);
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

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
        var model = taskStatusRepository.save(testModels.getFirst());

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
        var model = taskStatusRepository.save(testModels.getFirst());
        var request = delete("/api/task_statuses/{id}",
                model.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findBySlug(model.getSlug())).isEmpty();
        assertThat(taskStatusRepository.findById(model.getId())).isEmpty();
        assertThrows(ResourceNotFoundException.class, () -> service.getBySlug(model.getSlug()));
        assertThrows(ResourceNotFoundException.class, () -> service.getById(model.getId()));
    }

    @Test
    public void testDeleteTaskStatusWithoutAuth() throws Exception {
        var model = taskStatusRepository.save(testModels.getFirst());
        var request = delete("/api/task_statuses/{id}",
                model.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());

        assertThat(taskStatusRepository.findBySlug(model.getSlug())).isNotEmpty();
        assertThat(taskStatusRepository.findById(model.getId())).isNotEmpty();
    }

}
