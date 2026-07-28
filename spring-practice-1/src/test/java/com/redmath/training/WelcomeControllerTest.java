package com.redmath.training;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // Loads the entire application context (including Actuator)
@AutoConfigureMockMvc
public class WelcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnJsonInfo() {
        try {
            mockMvc.perform(get("/info"))
                    .andExpect(status().is2xxSuccessful())
                    .andDo(print())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.framework").value("Spring Boot 4.0.7"))
                    .andExpect(jsonPath("$.runtime").value("Java 25"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void shouldReturnNamedWelcome() {
        try {
            mockMvc.perform(get("/welcome/Ahmad"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Welcome Ahmad to Spring Boot"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void shouldReturnNamedWelcomeByQueryParam() {
        try {
            mockMvc.perform(get("/welcome?name=Ahmad"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Welcome Ahmad to Spring Boot"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void applicationHealthShouldBeUp() {
        try {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
