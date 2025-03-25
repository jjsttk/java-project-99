package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {
    @Email
    private String email;

    private String firstName;
    private String lastName;

    @Size(min = 3, max = 100)
    @NotBlank
    private String password;
}
