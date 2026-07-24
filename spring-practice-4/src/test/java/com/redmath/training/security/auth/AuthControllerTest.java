package com.redmath.training.security.auth;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(properties = "spring.boot.admin.client.enabled=false")
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void refresh_shouldReturnBadRequest_whenRefreshTokenIsMissing() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }

  @Test
  void refresh_shouldReturnBadRequest_whenRefreshTokenIsEmptyString() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Map.of("refresh_token", "").toString()))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
  }
}
