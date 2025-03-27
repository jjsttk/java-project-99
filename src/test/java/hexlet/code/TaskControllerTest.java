package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.component.specification.TaskSpecification;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.task.TaskService;
import hexlet.code.service.task.label.LabelService;
import hexlet.code.service.task.status.TaskStatusService;
import hexlet.code.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

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
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelService labelService;
    @Autowired
    private TaskSpecification specification;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    public void setup() {
        var fullFilledTask = buildFullFilledTestTaskModel();
        var onlyReqFieldsTask = buildOnlyReqTestTaskModel();
        tasks = List.of(fullFilledTask, onlyReqFieldsTask);
        clearAllDataInDB();
    }

    @Test
    public void modelsSaveTest() {
        saveTasksWithDependencies();

        var countAfterSave = taskRepository.count();
        var modelsSize = tasks.size();

        assertThat(countAfterSave).isEqualTo(modelsSize);
    }


    @Test
    public void testResourceNotFoundException() {
        var id = 9999L;
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> taskService.getById(id));
        assertThat(exception.getMessage()).isEqualTo("Task with id " + id + " not found");

    }

    @Test
    public void testIndexWithoutFiltersWithAuthorization() throws Exception {
        saveTasksWithDependencies();

        var result = mockMvc.perform(get("/api/tasks")
                        .with(jwt()))
                .andExpect(status().isOk()).andReturn();
        var body = result.getResponse().getContentAsString();

        var firstTestedModel = tasks.get(0);
        var secondTestedModel = tasks.get(1);

        var jsonIndexOfFirstTestedModel = "[0]";
        var jsonIndexOfSecondTestedModel = "[1]";

        assertThatJson(body).isArray().hasSize(2);
        assertThatJson(body).inPath(jsonIndexOfFirstTestedModel).and(
                v -> v.node("id").isEqualTo(firstTestedModel.getId()),
                v -> v.node("index").isEqualTo(firstTestedModel.getIndex()),
                v -> v.node("title").isEqualTo(firstTestedModel.getName()),
                v -> v.node("assignee_id").isEqualTo(firstTestedModel.getAssignee().getId()),
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
    public void testIndexWithAssigneeIdFilterWithAuthorization() throws Exception {
        saveTasksWithDependencies();

        var paramsDTO = new TaskParamsDTO();
        var notExistedAssigneeId = 9999L;
        paramsDTO.setAssigneeId(notExistedAssigneeId);

        var filter = specification.build(paramsDTO);
        var filteredTasks = taskRepository.findAll(filter);
        assertThat(filteredTasks).isEmpty();

        var request1 = get("/api/tasks")
                .param("assigneeId", String.valueOf(notExistedAssigneeId))
                .with(jwt());

        var response1 = mockMvc.perform(request1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body1 = response1.getContentAsString();
        assertThatJson(body1).isEqualTo("[]");


        var firstTaskAssigneeId = tasks.getFirst().getAssignee().getId();

        var anotherAssignee = buildTestAssignee("email@testtest.com", "password");
        userRepository.save(anotherAssignee);
        tasks.getLast().setAssignee(anotherAssignee);
        taskRepository.save(tasks.getLast());

        paramsDTO.setAssigneeId(firstTaskAssigneeId);
        filter = specification.build(paramsDTO);
        filteredTasks = taskRepository.findAll(filter);
        assertThat(filteredTasks).isNotEmpty();

        for (var task : filteredTasks) {
            assertThat(task.getAssignee().getId()).isEqualTo(paramsDTO.getAssigneeId());
        }

        var request2 = get("/api/tasks")
                .param("assigneeId", String.valueOf(firstTaskAssigneeId))
                .with(jwt());

        var response2 = mockMvc.perform(request2)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body2 = response2.getContentAsString();
        assertThatJson(body2).isArray().hasSize(1);
        assertThatJson(body2).node("[0].assignee_id").isEqualTo(paramsDTO.getAssigneeId());
    }

    @Test
    public void testIndexWithTaskStatusFilterWithAuthorization() throws Exception {
        saveTasksWithDependencies();

        var paramsDTO = new TaskParamsDTO();
        var taskStatusSlug = "to_publish";
        paramsDTO.setStatus(taskStatusSlug);

        var filter = specification.build(paramsDTO);

        var filteredTasks = taskRepository.findAll(filter);
        assertThat(filteredTasks).isEmpty();

        var requestWithBlankResult = get("/api/tasks")
                .param("status", taskStatusRepository.findBySlug(taskStatusSlug).toString())
                .with(jwt());

        var response = mockMvc.perform(requestWithBlankResult)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).isEqualTo("[]");

        var taskStatus = taskStatusRepository.save(buildTestTaskStatus(taskStatusSlug));
        tasks.getLast().setTaskStatus(taskStatus);
        taskRepository.save(tasks.getLast());

        filter = specification.build(paramsDTO);
        filteredTasks = taskRepository.findAll(filter);

        for (var task : filteredTasks) {
            assertThat(task.getTaskStatus().getSlug()).isEqualTo(paramsDTO.getStatus());
        }
    }

    @Test
    public void testIndexWithTitleFilterWithAuthorization() throws Exception {
        saveTasksWithDependencies();

        var paramsDTO = new TaskParamsDTO();
        paramsDTO.setTitleCont("In the end");
        var filter = specification.build(paramsDTO);
        var mbModel1 = taskRepository.findAll(filter);
        assertThat(mbModel1).isEmpty();
        var request1 = get("/api/tasks")
                .param("titleCont", paramsDTO.getTitleCont())
                .with(jwt());
        var response1 = mockMvc.perform(request1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body1 = response1.getContentAsString();
        assertThat(body1).isEqualTo("[]");


        var realTitle = tasks.getFirst().getName();
        var cutTitle = realTitle.length() > 2 ? realTitle.substring(1, realTitle.length() - 1) : "";
        paramsDTO.setTitleCont(cutTitle);
        filter = specification.build(paramsDTO);
        var mbModel2 = taskRepository.findAll(filter);
        assertThat(mbModel2).isNotEmpty();
        var request2 = get("/api/tasks")
                .param("titleCont", paramsDTO.getTitleCont())
                .with(jwt());
        var response2 = mockMvc.perform(request2)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body2 = response2.getContentAsString();
        assertThatJson(body2).isArray().hasSize(1);
        assertThatJson(body2).node("[0].title").isEqualTo(realTitle);
        assertThat(realTitle).contains(cutTitle);
    }

    @Test
    public void testIndexWithLabelFilterWithAuth() throws Exception {
        saveTasksWithDependencies();

        var paramsDTO = new TaskParamsDTO();
        paramsDTO.setLabelId(9999L);

        var filter1 = specification.build(paramsDTO);
        var mbModel1 = taskRepository.findAll(filter1);
        assertThat(mbModel1).isEmpty();

        var request1 = get("/api/tasks")
                .param("labelId", String.valueOf(paramsDTO.getLabelId()))
                .with(jwt());
        var response1 = mockMvc.perform(request1)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body1 = response1.getContentAsString();
        assertThat(body1).isEqualTo("[]");


        var model = tasks.getFirst();
        var label = labelRepository.save(buildTestLabel("test label"));
        model.addLabel(label);
        taskRepository.save(model);

        paramsDTO.setLabelId(label.getId());
        var filter2 = specification.build(paramsDTO);
        var mbModel2 = taskRepository.findAll(filter2);
        assertThat(mbModel2).isNotEmpty();

        var request2 = get("/api/tasks")
                .param("labelId", String.valueOf(paramsDTO.getLabelId()))
                .with(jwt());
        var response2 = mockMvc.perform(request2)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body2 = response2.getContentAsString();
        assertThatJson(body2).isArray().hasSize(1);
        assertThatJson(body2).node("[0].taskLabelIds").isEqualTo(Set.of(label.getId()));
    }

    @Test
    public void testIndexWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testShowWithAuthorization() throws Exception {
        saveTasksWithDependencies();
        var firstTestedModel = tasks.getFirst();
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
                v -> v.node("assignee_id").isEqualTo(firstTestedModel.getAssignee().getId()),
                v -> v.node("content").isEqualTo(firstTestedModel.getDescription()),
                v -> v.node("status").isEqualTo(firstTestedModel.getTaskStatus().getSlug()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testShowWithOutAuthorization() throws Exception {
        saveTasksWithDependencies();
        mockMvc.perform(get("/api/tasks/{id}", tasks.getFirst().getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testBadRequestCreateFromModelWithAuth() throws Exception {
        var model = tasks.getFirst();

        var request = post("/api/tasks")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(model));

        var response = mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateFromCreateDTO() throws Exception {
        saveTasksWithDependencies();
        var model = tasks.getFirst();
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
                v -> v.node("index").isEqualTo(createDTO.getIndex()),
                v -> v.node("title").isEqualTo(createDTO.getTitle()),
                v -> v.node("content").isEqualTo(createDTO.getContent()),
                v -> v.node("assignee_id").isEqualTo(createDTO.getAssigneeId()),
                v -> v.node("status").isEqualTo(createDTO.getStatus()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    public void testUpdateUsingSomeFieldsInUpdateDTO() throws Exception {
        saveTasksWithDependencies();
        var task = tasks.getFirst();
        var oldId = task.getId();
        var oldIndex = task.getIndex();
        var oldName = task.getName();
        var oldAssignee = task.getAssignee();
        var oldTaskStatus = task.getTaskStatus();
        var oldDescription = task.getDescription();

        var newTaskStatusModel = new TaskStatus();
        newTaskStatusModel.setSlug("updated_slug");
        newTaskStatusModel.setName("taskStatusNewName");
        taskStatusRepository.save(newTaskStatusModel);

        var updateDTO = new TaskUpdateDTO();
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
        assertThat(oldTaskStatus).isNotEqualTo(modelFromDB.getTaskStatus());

        assertThat(oldDescription).isEqualTo(modelFromDB.getDescription());
        assertThat(oldAssignee).isEqualTo(modelFromDB.getAssignee());
        assertThat(oldIndex).isEqualTo(modelFromDB.getIndex());

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(oldId),
                v -> v.node("index").isEqualTo(oldIndex),
                v -> v.node("title").isEqualTo(updateDTO.getTitle()),
                v -> v.node("content").isEqualTo(oldDescription),
                v -> v.node("assignee_id").isEqualTo(oldAssignee.getId()),
                v -> v.node("status").isEqualTo(updateDTO.getStatus()),
                v -> v.node("createdAt").isNotNull());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        saveTasksWithDependencies();
        var model = tasks.getFirst();

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
    public void testDeleteWithAuth() throws Exception {
        saveTasksWithDependencies();
        var task = tasks.getFirst();

        var request = delete("/api/tasks/{id}",
                task.getId()).with(jwt());

        mockMvc.perform(request).andExpect(status().isNoContent());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
        assertThat(taskStatusRepository.findBySlug(task.getTaskStatus().getSlug())).isPresent();
        assertThat(userRepository.findById(task.getAssignee().getId())).isPresent();

        assertThrows(ResourceNotFoundException.class, () -> taskService.getById(task.getId()));
    }

    @Test
    public void testDeleteWithoutAuth() throws Exception {
        saveTasksWithDependencies();
        var task = tasks.getFirst();

        var request = delete("/api/tasks/{id}",
                task.getId());

        mockMvc.perform(request).andExpect(status().isUnauthorized());
        assertThat(taskRepository.findById(task.getId())).isNotEmpty();
    }

    @Test
    public void testDeleteUserIfUserAssignedInTask() {
        saveTasksWithDependencies();
        var task = tasks.getFirst();
        var userId = task.getAssignee().getId();

        Exception exception = assertThrows(IllegalStateException.class, () -> userService.delete(userId));
        assertThat(exception.getMessage()).isEqualTo(
                "Cannot delete user with id = " + userId
                        + " ,because user was assigned to at least one task.");
    }

    @Test
    public void testDeleteUserIfUserNotAssignedInTask() {
        saveTasksWithDependencies();
        var task = tasks.getFirst();

        var userNotAssignedInTask = new User();
        userNotAssignedInTask.setEmail("qwe@test.com");
        userNotAssignedInTask.setPassword(passwordEncoder.encode("12345"));
        userRepository.save(userNotAssignedInTask);

        var notAssignedInTaskUserId = userNotAssignedInTask.getId();
        userRepository.deleteById(notAssignedInTaskUserId);
        assertThat(userRepository.findById(notAssignedInTaskUserId)).isEmpty();
    }



    @Test
    public void testDeleteTaskStatusIfAssignedInTask() {
        saveTasksWithDependencies();
        var task = tasks.getFirst();
        task.setAssignee(null);
        taskRepository.save(task);

        var taskStatusId = task.getTaskStatus().getId();
        Exception exception = assertThrows(IllegalStateException.class, () -> taskStatusService.delete(taskStatusId));
        assertThat(exception.getMessage()).isEqualTo(
                "Cannot delete task status with id = " + taskStatusId
                        + " , because it is used in at least one task.");
    }

    @Test
    public void testDeleteTaskStatusIfNotAssignedInTask() {
        saveTasksWithDependencies();

        var unBindedTaskStatus = new TaskStatus();
        unBindedTaskStatus.setName("test name");
        unBindedTaskStatus.setSlug("our_test_slug_to_test_delete");
        taskStatusRepository.save(unBindedTaskStatus);

        taskStatusService.delete(unBindedTaskStatus.getId());
        assertThat(taskStatusRepository.findById(unBindedTaskStatus.getId())).isEmpty();
    }

    @Test
    public void testDeleteLabelIfLabelAssignedInTask() {
        saveTasksWithDependencies();

        var task = tasks.getFirst();

        var testLabel = buildTestLabel("Assigned");
        var assignedLabel = labelRepository.save(testLabel);

        task.addLabel(assignedLabel);
        taskRepository.save(task);

        Exception exception = assertThrows(IllegalStateException.class,
                () -> labelService.delete(assignedLabel.getId()));
        assertThat(exception.getMessage()).isEqualTo(
                "Cannot delete label with id = " + assignedLabel.getId()
                        + " , because it is used in at least one task.");
    }

    @Test
    public void testDeleteLabelIfLabelNotAssignedInTask() {
        saveTasksWithDependencies();

        var label = buildTestLabel("Not assigned");
        labelRepository.save(label);

        labelService.delete(label.getId());
        assertThat(labelRepository.findById(label.getId())).isEmpty();
    }



    private Task buildFullFilledTestTaskModel() {
        var task1 = new Task();
        task1.setIndex(12);
        task1.setName("task1 name");
        task1.setDescription("description task1");
        task1.setAssignee(buildTestAssignee("test@email.ru", passwordEncoder.encode("qwerty")));
        task1.setTaskStatus(buildTestTaskStatus("to_review_test"));
        return task1;
    }

    private Task buildOnlyReqTestTaskModel() {
        var task2 = new Task();
        task2.setName("task2 name");
        task2.setTaskStatus(buildTestTaskStatus("draft_test"));
        return task2;
    }

    private User buildTestAssignee(String email, String password) {
        var user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }

    private TaskStatus buildTestTaskStatus(String slug) {
        var taskStatus = new TaskStatus();
        taskStatus.setName("testTaskStatus");
        taskStatus.setSlug(slug);
        return taskStatus;
    }

    private Label buildTestLabel(String name) {
        var label = new Label();
        label.setName(name);
        return label;
    }

    private void clearAllDataInDB() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void saveTasksWithDependencies() {
        for (var task : tasks) {
            if (task.getAssignee() != null) {
                userRepository.save(task.getAssignee());
            }
            taskStatusRepository.save(task.getTaskStatus());
            taskRepository.save(task);
        }
    }
}
