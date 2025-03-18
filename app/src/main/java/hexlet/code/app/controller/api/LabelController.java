package hexlet.code.app.controller.api;

import hexlet.code.app.dto.task.label.LabelCreateDTO;
import hexlet.code.app.dto.task.label.LabelDTO;
import hexlet.code.app.dto.task.label.LabelUpdateDTO;
import hexlet.code.app.service.task.label.LabelService;
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
 * REST controller for managing labels.
 */
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class LabelController {
    private LabelService service;

    /**
     * Retrieves all labels.
     *
     * @return a list of {@link LabelDTO} with a total count in the response header.
     */
    @GetMapping("/labels")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LabelDTO>> index() {
        var listDTO = service.getAll();
        var count = service.totalCount();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(count))
                .body(listDTO);
    }

    /**
     * Retrieves a specific label by its ID.
     *
     * @param id the ID of the label
     * @return the {@link LabelDTO} of the requested label
     */
    @GetMapping("/labels/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO show(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * Creates a new label.
     *
     * @param createDTO the label data for creation
     * @return the created {@link LabelDTO}
     */
    @PostMapping("/labels")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@RequestBody @Valid LabelCreateDTO createDTO) {
        return service.create(createDTO);
    }

    /**
     * Updates an existing task.
     *
     * @param id the ID of the label to update
     * @param updateDTO the updated label data
     * @return the updated {@link LabelDTO}
     */
    @PutMapping("/labels/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO update(@PathVariable Long id, @RequestBody @Valid LabelUpdateDTO updateDTO) {
        return service.update(updateDTO, id);
    }

    /**
     * Deletes a label by its ID.
     *
     * @param id the ID of the label to delete
     */
    @DeleteMapping("/labels/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

}
