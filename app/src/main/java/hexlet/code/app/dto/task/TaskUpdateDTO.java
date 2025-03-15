package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.openapitools.jackson.nullable.JsonNullable;


@Data
public class TaskUpdateDTO {
    private JsonNullable<Integer> index;
    private JsonNullable<Long> assigneeId;

    @NotBlank
    @Length(min = 1)
    private JsonNullable<String> title;

    private JsonNullable<String> content;
    private JsonNullable<String> status;
}
