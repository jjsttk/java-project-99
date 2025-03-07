package hexlet.code.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {

    private String firstName;
    private String lastName;

    @Email
    private String email;

    @Size(min = 3)
    @NotBlank
    private String password;
}
