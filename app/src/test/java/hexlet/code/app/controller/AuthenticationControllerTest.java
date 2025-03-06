package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.auth.AuthRequest;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Log4j2
@AutoConfigureMockMvc
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testLogin() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("hexlet@example.com");
        authRequest.setPassword("qwerty");

        var request = post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isNotBlank();

        var jwtRegex = "^[^.]+\\.[^.]+\\.[^.]+$";
        assertThat(response).matches(jwtRegex);
    }

    @Test
    public void testLoginWithoutUserInDB() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("asdasd");
        authRequest.setPassword("asd123123");

        var request = post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest));

        var response = mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

    }
}
