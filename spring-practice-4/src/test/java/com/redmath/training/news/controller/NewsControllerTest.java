package com.redmath.training.news.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NewsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void findAll_withoutPageParams_shouldReturnFirstPageWithDefaults() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].newsId").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[9].newsId").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value(0))
        .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(20))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2));
  }

  @Test
  void findAll_withPageParams_shouldReturnRequestedPageSortedAccordingly() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news")
            .param("page", "1")
            .param("size", "5")
            .param("sortBy", "newsId")
            .param("direction", "asc"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(5))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].newsId").value(6))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[4].newsId").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.currentPage").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(5))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(20))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(4));
  }

  @Test
  void findById_shouldReturnNews_whenNewsExists() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news/1"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.newsId").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Spring Boot 4.0 Released"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.reportedBy").value("reporter1"));
  }

  @Test
  void findById_shouldReturnNotFound_whenNewsDoesNotExist() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news/9999"))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void create_shouldReturnCreatedNews_whenUserIsAuthenticated() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/news")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Test Title\", \"description\": \"Test description\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter1")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Title"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Test description"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.reportedBy").value("reporter1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.newsId").value(Matchers.greaterThan(0)));
  }

  @Test
  void create_shouldReturnForbidden_whenUserIsNotAuthenticated() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/news")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Test Title\", \"description\": \"Test description\"}"))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  void create_shouldReturnForbidden_whenCsrfTokenIsMissing() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/news")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Test Title\", \"description\": \"Test description\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter1")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter"))))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  void update_shouldReturnUpdatedNews_whenUserIsOwner() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/news/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Updated Title\", \"description\": \"Updated description\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter1")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Updated Title"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Updated description"));
  }

  @Test
  void update_shouldReturnUpdatedNews_whenUserIsEditor() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/news/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Updated by Editor\", \"description\": \"Updated description\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("editor")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("editor")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Updated by Editor"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Updated description"));
  }

  @Test
  void update_shouldReturnForbidden_whenUserIsNotOwnerNorEditor() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/news/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Hacked Title\", \"description\": \"Hacked description\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter2")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  void update_shouldReturnNotFound_whenNewsDoesNotExist() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/news/9999")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Updated Title\", \"description\": \"Updated description\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter1")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void partialUpdate_shouldPreserveUnchangedFields_whenUserIsOwner() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/news/2")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"New Title Only\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter2")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("New Title Only"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description")
            .value(
                "How production-grade AI systems are shifting the landscape of traditional indexing."));
  }

  @Test
  void partialUpdate_shouldReturnUpdatedNews_whenUserIsEditor() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/news/2")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Editor Patched Title\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("editor")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("editor")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Editor Patched Title"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description")
            .value(
                "How production-grade AI systems are shifting the landscape of traditional indexing."));
  }

  @Test
  void partialUpdate_shouldReturnForbidden_whenUserIsNotOwnerNorEditor() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/news/2")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Hacked Title\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter1")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  void partialUpdate_shouldReturnNotFound_whenNewsDoesNotExist() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/news/9999")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"Patched Title\"}")
            .with(SecurityMockMvcRequestPostProcessors.user("reporter1")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("reporter")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_shouldReturnNoContent_whenUserIsEditor() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/news/1")
            .with(SecurityMockMvcRequestPostProcessors.user("editor")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("editor")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_shouldReturnNotFound_whenNewsDoesNotExist() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/news/9999")
            .with(SecurityMockMvcRequestPostProcessors.user("editor")
                .authorities(AuthorityUtils.commaSeparatedStringToAuthorityList("editor")))
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnNewsAfterGivenDate() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news/after")
            .param("date", "2026-07-01")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
