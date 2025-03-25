package hexlet.code.app.dto.task;

import lombok.Data;

@Data
public class TaskParamsDTO {
    private String titleCont;
    private Long assigneeId;
    private String status;
    private Long labelId;
}
