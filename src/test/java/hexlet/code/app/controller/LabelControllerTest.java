package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.task.label.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.service.task.label.LabelService;
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

import java.util.List;

@Log4j2
@SpringBootTest
@AutoConfigureMockMvc
class LabelControllerTest {
    private List<Label> labels;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator generator;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private LabelService labelService;


    @BeforeEach
    public void setup() {
        labels = Instancio.of(generator.getLabels()).create();
    }

    @Test
    @Transactional
    public void testIndexWithAuth() throws Exception {
        var countBeforeSave = labelRepository.count();
        var savedLabels = labelRepository.saveAll(labels);
        var totalSize = countBeforeSave + savedLabels.size();
        var request = get("/api/labels").with(jwt());
        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        assertThatJson(body).isArray().hasSize((int) totalSize);
    }

    @Test
    @Transactional
    public void testIndexWithoutAuth() throws Exception {
        labelRepository.saveAll(labels);
        var request = get("/api/labels");
        var response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testResourceNotFoundException() {
        var id = 9999L;
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> labelService.getById(id));
        assertThat(exception.getMessage()).isEqualTo("Label with id " + id + " not found");
    }

    @Test
    @Transactional
    public void testShowWithAuth() throws Exception {
        var model = labels.getFirst();
        var saved = labelRepository.save(model);
        assertThat(model.getName()).isEqualTo(saved.getName());

        var request = get("/api/labels/{id}", model.getId()).with(jwt());
        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        assertThatJson(body).isObject();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(model.getId()),
                v -> v.node("name").isEqualTo(model.getName()),
                v -> v.node("createdAt").isNotNull()
        );
    }
    @Test
    @Transactional
    public void testShowWithoutAuth() throws Exception {
        var model = labels.getFirst();
        var saved = labelRepository.save(model);
        assertThat(model.getName()).isEqualTo(saved.getName());

        var request = get("/api/labels/{id}", model.getId());
        var response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();
    }

    @Test
    @Transactional
    public void testCreateWithoutAuth() throws Exception {
        var model = labels.getFirst();
        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(model));
        var response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();
    }

    @Test
    @Transactional
    public void testCreateWithAuth() throws Exception {
        var model = labels.getFirst();
        assertThat(labelRepository.findByName(model.getName())).isEmpty();

        var request = post("/api/labels")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(model));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        var mbModelFromDB = labelRepository.findByName(model.getName());
        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(modelFromDB.getId()),
                v -> v.node("name").isEqualTo(modelFromDB.getName()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testUpdateWithoutAuth() throws Exception {
        var model = labels.getFirst();
        var savedModel = labelRepository.save(model);
        assertThat(labelRepository.findByName(savedModel.getName())).isPresent();

        var updateDTO = new LabelUpdateDTO();
        updateDTO.setName(JsonNullable.of("newTestName"));

        var request = put("/api/labels/{id}", savedModel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();
    }

    @Test
    @Transactional
    public void testUpdateWithAuth() throws Exception {
        var model = labels.getFirst();
        var savedModel = labelRepository.save(model);
        assertThat(labelRepository.findByName(savedModel.getName())).isPresent();

        var updateDTO = new LabelUpdateDTO();
        updateDTO.setName(JsonNullable.of("newTestName"));

        var request = put("/api/labels/{id}", savedModel.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var body = response.getContentAsString();

        var mbModelFromDB = labelRepository.findByName(updateDTO.getName().get());

        assertThat(mbModelFromDB).isPresent();
        var modelFromDB = mbModelFromDB.get();

        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(savedModel.getId()),
                v -> v.node("name").isEqualTo(updateDTO.getName().get()),
                v -> v.node("createdAt").isNotNull()
        );
    }

    @Test
    @Transactional
    public void testDeleteWithoutAuth() throws Exception {
        var model = labels.getFirst();
        var savedModel = labelRepository.save(model);
        assertThat(labelRepository.findByName(savedModel.getName())).isPresent();

        var request = delete("/api/labels/{id}", savedModel.getId());
        var response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();
        assertThat(labelRepository.findById(savedModel.getId())).isPresent();
    }

    @Test
    @Transactional
    public void testDeleteWithAuth() throws Exception {
        var model = labels.getFirst();
        var savedModel = labelRepository.save(model);
        assertThat(labelRepository.findByName(savedModel.getName())).isPresent();

        var request = delete("/api/labels/{id}",
                savedModel.getId()).with(jwt());
        var response = mockMvc.perform(request)
                .andExpect(status().isNoContent())
                .andReturn()
                .getResponse();

        assertThat(labelRepository.findById(savedModel.getId())).isEmpty();
        assertThat(labelRepository.findByName(savedModel.getName())).isEmpty();
    }
}
