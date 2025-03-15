package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.task.TaskService;
import hexlet.code.app.service.task.status.TaskStatusService;
import hexlet.code.app.service.user.UserService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {
    private List<Task> tasks;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;
    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private UserRepository userRepository;



    @BeforeEach
    public void setup() {
        var fullFilledTask = createFullFilledTestTaskModel();
        var onlyReqFieldsTask = createOnlyReqTestTaskModel();
        tasks = new ArrayList<>(List.of(fullFilledTask, onlyReqFieldsTask));
    }

    @Test
    @Transactional
    public void modelsSaveTest() {
        var countBeforeSave = taskRepository.count();

        for (var task : tasks) {
            if (task.getAssignee() != null) {
                userRepository.save(task.getAssignee());
            }
            if (task.getTaskStatus() != null) {
                taskStatusRepository.save(task.getTaskStatus());
            }
            taskRepository.save(task);
        }

        var countAfterSave = taskRepository.count();
        var modelsSize = tasks.size();

        assertThat(countAfterSave - countBeforeSave).isEqualTo(modelsSize);
    }


    @Test
    public void testResourceNotFoundException() {
        var id = 9999L;
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> taskService.getById(id));
        assertThat(exception.getMessage()).isEqualTo("Task with id " + id + " not found");

    }

    @Test
    @Transactional
    public void testIndexWithAuthorization() throws Exception {
        var countBeforeAddTestModels = taskRepository.count();

        for (var task : tasks) {
            if (task.getAssignee() != null) {
                userRepository.save(task.getAssignee());
            }
            if (task.getTaskStatus() != null) {
                taskStatusRepository.save(task.getTaskStatus());
            }
            taskRepository.save(task);
        }

        var countAfterAddTestModels = (int) taskRepository.count();

        var result = mockMvc.perform(get("/api/tasks")
                        .with(jwt()))
                .andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        var firstTestedModel = tasks.get(0);
        var secondTestedModel = tasks.get(1);

        var jsonIndexOfFirstTestedModel = String.format("[%d]", countBeforeAddTestModels);
        var jsonIndexOfSecondTestedModel = String.format("[%d]", countBeforeAddTestModels + 1);

        assertThat(countBeforeAddTestModels).isNotEqualTo(countAfterAddTestModels);
        assertThat(countAfterAddTestModels - tasks.size()).isEqualTo(countBeforeAddTestModels);
        assertThatJson(body).isArray().hasSize(countAfterAddTestModels);
        assertThatJson(body).inPath(jsonIndexOfFirstTestedModel).and(
                v -> v.node("id").isEqualTo(firstTestedModel.getId()),
                v -> v.node("index").isEqualTo(firstTestedModel.getIndex()),
                v -> v.node("title").isEqualTo(firstTestedModel.getName()),
                v -> v.node("assigneeId").isEqualTo(firstTestedModel.getAssignee().getId()),
                v -> v.node("content").isEqualTo(firstTestedModel.getDescription()),
                v -> v.node("status").isEqualTo(firstTestedModel.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isNotNull()
        );

        assertThatJson(body).inPath(jsonIndexOfSecondTestedModel).and(
                v -> v.node("id").isEqualTo(secondTestedModel.getId()),
                v -> v.node("title").isEqualTo(secondTestedModel.getName()),
                v -> v.node("status").isEqualTo(secondTestedModel.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }



    @Test
    public void testIndexWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testShowWithAuthorization() throws Exception {
        var firstTestedModel = tasks.getFirst();
        userRepository.save(firstTestedModel.getAssignee());
        taskStatusRepository.save(firstTestedModel.getTaskStatus());
        taskRepository.save(firstTestedModel);

        var resultBody = mockMvc.perform(get("/api/tasks/{id}", firstTestedModel.getId())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(resultBody).and(
                v -> v.node("id").isEqualTo(firstTestedModel.getId()),
                v -> v.node("index").isEqualTo(firstTestedModel.getIndex()),
                v -> v.node("title").isEqualTo(firstTestedModel.getName()),
                v -> v.node("assigneeId").isEqualTo(firstTestedModel.getAssignee().getId()),
                v -> v.node("content").isEqualTo(firstTestedModel.getDescription()),
                v -> v.node("status").isEqualTo(firstTestedModel.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testShowWithOutAuthorization() throws Exception {
        var firstTestedModel = tasks.getFirst();
        userRepository.save(firstTestedModel.getAssignee());
        taskStatusRepository.save(firstTestedModel.getTaskStatus());
        taskRepository.save(firstTestedModel);

        mockMvc.perform(get("/api/tasks/{id}", firstTestedModel.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testCreateNewTaskFromModelWithAuth() throws Exception {
        var model = tasks.getFirst();

        var request = post("/api/tasks")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(model));

        var response = mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void testCreateNewTaskFromModelWithOutAuth() throws Exception {
        var model = tasks.getFirst();

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(model));

        var result = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testCreateNewTaskFromCreateDTO() throws Exception {
        var model = tasks.getFirst();
        taskStatusRepository.save(model.getTaskStatus());
        userRepository.save(model.getAssignee());

        var createDTO = new TaskCreateDTO();
        createDTO.setIndex(model.getIndex());
        createDTO.setAssigneeId(model.getAssignee().getId());
        createDTO.setTitle(model.getName());
        createDTO.setContent(model.getDescription());
        createDTO.setStatus(model.getTaskStatus().getSlug());

        var request = post("/api/tasks")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        var body = response.getContentAsString();
        var id = om.readTree(body).get("id").asLong();
        var mbModelFromDB = taskRepository.findById(id);
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThat(createDTO.getStatus()).isEqualTo(modelFromDB.getTaskStatus().getSlug());
        assertThat(createDTO.getIndex()).isEqualTo(modelFromDB.getIndex());
        assertThat(createDTO.getContent()).isEqualTo(modelFromDB.getDescription());
        assertThat(createDTO.getTitle()).isEqualTo(modelFromDB.getName());
        assertThat(createDTO.getAssigneeId()).isEqualTo(modelFromDB.getAssignee().getId());
        assertThat(modelFromDB.getCreatedAt()).isNotNull();


        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelFromDB.getId()),
                v -> v.node("index").isEqualTo(modelFromDB.getIndex()),
                v -> v.node("title").isEqualTo(modelFromDB.getName()),
                v -> v.node("content").isEqualTo(modelFromDB.getDescription()),
                v -> v.node("assigneeId").isEqualTo(modelFromDB.getAssignee().getId()),
                v -> v.node("status").isEqualTo(modelFromDB.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testUpdateTaskUsingSomeFieldsInUpdateDTO() throws Exception {
        var task = tasks.getFirst();
        userRepository.save(task.getAssignee());
        taskStatusRepository.save(task.getTaskStatus());
        taskRepository.save(task);

        var oldId = task.getId();
        var oldIndex = task.getIndex();
        var oldName = task.getName();
        var oldAssignee = task.getAssignee();
        var oldTaskStatus = task.getTaskStatus();
        var oldDescription = task.getDescription();

        var updateDTO = new TaskUpdateDTO();
        var newTaskStatusModel = new TaskStatus();

        newTaskStatusModel.setSlug("updated_slug");
        newTaskStatusModel.setName("taskStatusNewName");
        taskStatusRepository.save(newTaskStatusModel);

        updateDTO.setStatus(JsonNullable.of(newTaskStatusModel.getSlug()));
        updateDTO.setTitle(JsonNullable.of("new title"));

        var request = put("/api/tasks/{id}", oldId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var body = response.getContentAsString();

        var mbModelFromDB = taskRepository.findById(oldId);
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThat(oldName).isNotEqualTo(modelFromDB.getName());
        assertThat(oldDescription).isEqualTo(modelFromDB.getDescription());
        assertThat(oldTaskStatus).isNotEqualTo(modelFromDB.getTaskStatus());
        assertThat(oldAssignee).isEqualTo(modelFromDB.getAssignee());
        assertThat(oldIndex).isEqualTo(modelFromDB.getIndex());

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelFromDB.getId()),
                v -> v.node("index").isEqualTo(modelFromDB.getIndex()),
                v -> v.node("title").isEqualTo(modelFromDB.getName()),
                v -> v.node("content").isEqualTo(modelFromDB.getDescription()),
                v -> v.node("assigneeId").isEqualTo(modelFromDB.getAssignee().getId()),
                v -> v.node("status").isEqualTo(modelFromDB.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isNotNull());
    }

    @Test
    @Transactional
    public void testUpdateTaskWithoutAuth() throws Exception {
        var model = tasks.getFirst();
        userRepository.save(model.getAssignee());
        taskStatusRepository.save(model.getTaskStatus());
        taskRepository.save(model);

        var updateDTO = new TaskUpdateDTO();
        var newName = "Updated name";
        updateDTO.setTitle(JsonNullable.of(newName));

        var request = put("/api/tasks/{id}", model.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testDeleteTaskWithAuth() throws Exception {
        var task = tasks.getFirst();
        userRepository.save(task.getAssignee());
        taskStatusRepository.save(task.getTaskStatus());
        taskRepository.save(task);

        var request = delete("/api/tasks/{id}",
                task.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
        assertThat(taskStatusRepository.findBySlug(task.getTaskStatus().getSlug())).isPresent();
        assertThat(userRepository.findById(task.getAssignee().getId())).isPresent();

        assertThrows(ResourceNotFoundException.class, () -> taskService.getById(task.getId()));
    }

    @Test
    @Transactional
    public void testDeleteTaskWithoutAuth() throws Exception {
        var task = tasks.getFirst();
        userRepository.save(task.getAssignee());
        taskStatusRepository.save(task.getTaskStatus());
        taskRepository.save(task);

        var request = delete("/api/tasks/{id}",
                task.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());

        assertThat(taskRepository.findById(task.getId())).isNotEmpty();
    }

    @Test
    @Transactional
    public void testDeleteUserIfUserAssignedInTask() {
        var task = tasks.getFirst();
        userRepository.save(task.getAssignee());
        taskStatusRepository.save(task.getTaskStatus());
        taskRepository.save(task);

        var userId = task.getAssignee().getId();

        Exception exception = assertThrows(IllegalStateException.class, () -> userService.delete(userId));
        assertThat(exception.getMessage()).isEqualTo(
                "Cannot delete user with id = " + userId
                        + " ,because user was assigned to at least one task.");
    }

    @Test
    @Transactional
    public void testDeleteUserIfUserNotAssignedInTask() {
        var task = tasks.getFirst();
        userRepository.save(task.getAssignee());
        taskStatusRepository.save(task.getTaskStatus());
        taskRepository.save(task);

        var userNotAssignedInTask = new User();
        userNotAssignedInTask.setEmail("qwe@test.com");
        userNotAssignedInTask.setPassword("12345");
        userRepository.save(userNotAssignedInTask);

        var userIdNotAssignedInTask = userNotAssignedInTask.getId();
        var userIdAssignedInTask = task.getAssignee().getId();

        Exception exception = assertThrows(IllegalStateException.class, () -> userService.delete(userIdAssignedInTask));
        assertThat(exception.getMessage()).isEqualTo(
                "Cannot delete user with id = " + userIdAssignedInTask
                        + " ,because user was assigned to at least one task.");

        var userNotAssignedInTaskFromDB = userRepository.findById(userIdNotAssignedInTask);
        assertThat(userNotAssignedInTaskFromDB).isPresent();
        assertThat(userNotAssignedInTaskFromDB.get().getEmail()).isEqualTo("qwe@test.com");
        userRepository.deleteById(userIdNotAssignedInTask);
        assertThat(userRepository.findById(userIdNotAssignedInTask)).isEmpty();
    }



    @Test
    @Transactional
    public void testDeleteTaskStatusIfTaskStatusAssignedInTask() {
        var task = tasks.getFirst();
        task.setAssignee(null);

        taskStatusRepository.save(task.getTaskStatus());
        log.info("this status in task: {}", task.getTaskStatus().toString());
        taskRepository.save(task);

        var taskStatusId = task.getTaskStatus().getId();
        Exception exception = assertThrows(IllegalStateException.class, () -> taskStatusService.delete(taskStatusId));
        assertThat(exception.getMessage()).isEqualTo(
                "Cannot delete task status with id = " + taskStatusId
                        + " , because it is used in at least one task.");
    }

    @Test
    @Transactional
    public void testDeleteTaskStatusIfTaskStatusNotAssignedInTask() {
        var task = tasks.getFirst();
        task.setAssignee(null);

        taskStatusRepository.save(task.getTaskStatus());

        var unBindedTaskStatus = new TaskStatus();
        unBindedTaskStatus.setName("test name");
        unBindedTaskStatus.setSlug("our_test_slug_to_delete");
        taskStatusRepository.save(unBindedTaskStatus);
        taskRepository.save(task);

        var taskStatusId = task.getTaskStatus().getId();
        Exception exception = assertThrows(IllegalStateException.class, () -> taskStatusService.delete(taskStatusId));
        assertThat(exception.getMessage()).isEqualTo(
                "Cannot delete task status with id = " + taskStatusId
                        + " , because it is used in at least one task.");

        assertThat(taskStatusRepository.findById(unBindedTaskStatus.getId())).isPresent();
        assertThat(task.getTaskStatus().getId()).isNotEqualTo(unBindedTaskStatus.getId());

        taskStatusService.delete(unBindedTaskStatus.getId());
        assertThat(taskStatusRepository.findById(unBindedTaskStatus.getId())).isEmpty();
    }






    private Task createFullFilledTestTaskModel() {
        var task1 = new Task();
        task1.setIndex(12);
        task1.setName("task1 name");
        task1.setDescription("description task1");
        task1.setAssignee(getTestAssignee("test@email.ru", "qwerty"));
        task1.setTaskStatus(getTestTaskStatus("to_review_test"));
        return task1;
    }

    private Task createOnlyReqTestTaskModel() {
        var task2 = new Task();
        task2.setName("task2 name");
        task2.setTaskStatus(getTestTaskStatus("draft_test"));
        return task2;
    }



    private User getTestAssignee(String email, String password) {
        var mbUser = userRepository.findByEmail(email);
        if (mbUser.isEmpty()) {
            var user = new User();
            user.setEmail(email);
            user.setPassword(password);
            return user;
        } else {
            return mbUser.get();
        }
    }

    private TaskStatus getTestTaskStatus(String slug) {
        var mbTaskStatus = taskStatusRepository.findBySlug(slug);
        if (mbTaskStatus.isEmpty()) {
            var taskStatus = new TaskStatus();
            taskStatus.setName("testTaskStatus");
            taskStatus.setSlug(slug);
            return taskStatus;
        } else {
            return mbTaskStatus.get();
        }
    }
}
