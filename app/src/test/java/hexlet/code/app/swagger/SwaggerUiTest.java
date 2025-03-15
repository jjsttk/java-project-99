package hexlet.code.app.swagger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwaggerUiTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetSwaggerUiPage() throws Exception {
        var url1 = "/swagger-ui/index.html";
        var url2 = "/swagger-ui.html";
        var url3 = "/v3/api-docs";

        mockMvc.perform(get(url1)).andExpect(status().isOk());

        mockMvc.perform(get(url2)).andExpect(status().isFound()); //redirect

        var body = mockMvc.perform(get(url3))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThatJson(body).isObject();
    }
}
