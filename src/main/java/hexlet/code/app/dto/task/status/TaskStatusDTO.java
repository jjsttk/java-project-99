package hexlet.code.app.dto.task.status;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskStatusDTO {
    private Long id;
    private String name;
    private String slug;
    private LocalDate createdAt;
}
