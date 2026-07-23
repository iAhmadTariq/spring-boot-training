package com.redmath.training.welcome;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(properties = "spring.boot.admin.client.enabled=false")
@AutoConfigureMockMvc
class WelcomeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldWelcome() {
    try {
      mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/welcome").with(
                  SecurityMockMvcRequestPostProcessors.user("reporter")
                      .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
              .with(SecurityMockMvcRequestPostProcessors.csrf()))
          .andDo(print())
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(
              MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Default Welcome"));
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}
