package com.redmath.training.welcome;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(properties = {"spring.boot.admin.client.enabled=false",
    "app.message.welcome=Custom Welcome"})
@AutoConfigureMockMvc
class WelcomeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturnCustomWelcomeMessage_whenPropertyIsSet() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/welcome"))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content()
                .contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Custom Welcome"));
  }

  @Test
  void shouldReturnDefaultWelcomeMessage_whenPropertyIsNotSet() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/welcome"))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content()
                .contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Custom Welcome"));
  }
}
