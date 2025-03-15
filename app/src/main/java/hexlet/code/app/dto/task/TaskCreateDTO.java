package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class TaskCreateDTO {
    private Integer index;
    private Long assigneeId;

    @NotBlank
    @Length(min = 1)
    private String title;

    private String content;
    private String status;
}
