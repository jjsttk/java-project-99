package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.Set;


@Data
public class TaskUpdateDTO {
    private JsonNullable<Set<Long>> taskLabelIds;
    private JsonNullable<Integer> index;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    @NotBlank
    @Length(min = 1)
    private JsonNullable<String> title;

    private JsonNullable<String> content;
    private JsonNullable<String> status;
}
