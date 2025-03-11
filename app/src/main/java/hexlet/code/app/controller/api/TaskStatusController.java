package hexlet.code.app.controller.api;

import hexlet.code.app.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task.status.TaskStatusUpdateDTO;
import hexlet.code.app.service.task.status.TaskStatusService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import hexlet.code.app.dto.task.status.TaskStatusDTO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing task statuses.
 * Provides endpoints for CRUD operations on task statuses.
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class TaskStatusController {
    private final TaskStatusService service;

    /**
     * Retrieves a list of all task statuses.
     *
     * @return ResponseEntity containing a list of TaskStatusDTO objects and the total count in the header.
     */
    @GetMapping("/task_statuses")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var listDTO = service.getAll();
        var count = service.totalCount();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(count))
                .body(listDTO);
    }

    /**
     * Retrieves a specific task status by its ID.
     *
     * @param id the ID of the task status.
     * @return the TaskStatusDTO object corresponding to the given ID.
     */
    @GetMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO show(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * Creates a new task status.
     *
     * @param createDTO the DTO containing task status creation data.
     * @return the created TaskStatusDTO object.
     */
    @PostMapping("/task_statuses")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(@RequestBody @Valid TaskStatusCreateDTO createDTO) {
        return service.create(createDTO);
    }

    /**
     * Updates an existing task status.
     *
     * @param id        the ID of the task status to update.
     * @param updateDTO the DTO containing updated task status data.
     * @return the updated TaskStatusDTO object.
     */
    @PutMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO update(@PathVariable Long id, @RequestBody @Valid TaskStatusUpdateDTO updateDTO) {
        return service.update(updateDTO, id);
    }

    /**
     * Deletes a task status by its ID.
     *
     * @param id the ID of the task status to delete.
     */
    @DeleteMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
