package com.redmath.training.news.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@SpringBootTest
@AutoConfigureMockMvc
public class NewsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void findAll_withoutPageParams_shouldReturnFirstPageWithDefaults() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news"))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].newsId").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[9].newsId").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageNumber").value(0))
        .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(20))
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
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(5))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].newsId").value(6))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[4].newsId").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$.pageable.pageNumber").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(5))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(20))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(4));
  }

  @Test
  void shouldCreateNews() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/news")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"author\":\"test author\", \"title\": \"test title\", \"description\": \"test description\"}"))
        .andDo(print())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.author").value("test author"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("test title"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("test description"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.reportedAt").value(Matchers.notNullValue()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.newsId").value(Matchers.notNullValue())
        );
  }
}
