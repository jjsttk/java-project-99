package hexlet.code.app.dto.task;

import hexlet.code.app.model.Label;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private List<Long> taskLabelIds;
    private Integer index;
    private LocalDateTime createdAt;
    private Long assigneeId;
    private String title;
    private String content;
    private String status;
}
