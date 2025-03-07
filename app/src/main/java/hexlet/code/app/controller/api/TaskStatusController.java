package hexlet.code.app.controller.api;

import hexlet.code.app.service.task.status.TaskStatusService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hexlet.code.app.dto.task.status.TaskStatusDTO;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Log4j2
public class TaskStatusController {
    private final TaskStatusService service;

    @GetMapping("/task_statuses")
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var listDTO = service.getAll();
        var count = service.getTotalCount();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(count))
                .body(listDTO);
    }
}
