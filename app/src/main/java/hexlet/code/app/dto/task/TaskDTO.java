package hexlet.code.app.dto.task;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskDTO {
    private Long id;
    private Integer index;
    private LocalDate createdAt;
    private Long assigneeId;
    private String title;
    private String content;
    private String status;
}
