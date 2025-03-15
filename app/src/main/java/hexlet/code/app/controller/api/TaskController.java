package hexlet.code.app.controller.api;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskParamsDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.service.task.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * REST controller for managing tasks.
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class TaskController {
    private final TaskService service;

    /**
     * Retrieves all tasks.
     *
     * @param params contains filter params;
     * @return a list of {@link TaskDTO} with a total count in the response header.
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> index(TaskParamsDTO params) {
        var listDTO = service.getAll(params);
        var count = service.totalCount();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(count))
                .body(listDTO);
    }

    /**
     * Retrieves a specific task by its ID.
     *
     * @param id the ID of the task
     * @return the {@link TaskDTO} of the requested task
     */
    @GetMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO show(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * Creates a new task.
     *
     * @param createDTO the task data for creation
     * @return the created {@link TaskDTO}
     */
    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@RequestBody @Valid TaskCreateDTO createDTO) {
        return service.create(createDTO);
    }

    /**
     * Updates an existing task.
     *
     * @param id the ID of the task to update
     * @param updateDTO the updated task data
     * @return the updated {@link TaskDTO}
     */
    @PutMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO update(@PathVariable Long id, @RequestBody @Valid TaskUpdateDTO updateDTO) {
        return service.update(updateDTO, id);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the ID of the task to delete
     */
    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
