package hexlet.code.app.dto.task.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
public class LabelUpdateDTO {

    @NotBlank
    @Size(min = 3, max = 1000)
    private JsonNullable<String> name;
}
