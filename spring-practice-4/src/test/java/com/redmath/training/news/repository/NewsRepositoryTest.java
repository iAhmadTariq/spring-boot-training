package com.redmath.training.news.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.redmath.training.news.model.News;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NewsRepositoryTest {

  @Mock
  private NewsRepository newsRepository;

  @Test
  void findNewsAfterThisDate_shouldReturnNewsAfterDate() {
    News newNews = new News();
    newNews.setTitle("New News");
    newNews.setDescription("New description");
    newNews.setReportedBy("reporter2");
    newNews.setReportedAt(LocalDateTime.now().minusDays(1));

    org.mockito.Mockito.when(
            newsRepository.findNewsAfterThisDate(org.mockito.ArgumentMatchers.<LocalDateTime>any()))
        .thenReturn(List.of(newNews));

    List<News> result = newsRepository.findNewsAfterThisDate(LocalDateTime.now().minusDays(2));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("New News");
  }

  @Test
  void findNewsAfterThisDate_shouldReturnEmpty_whenNoNewsAfterDate() {
    org.mockito.Mockito.when(
            newsRepository.findNewsAfterThisDate(org.mockito.ArgumentMatchers.<LocalDateTime>any()))
        .thenReturn(List.of());

    List<News> result = newsRepository.findNewsAfterThisDate(LocalDateTime.now().minusDays(1));

    assertThat(result).isEmpty();
  }

  @Test
  void findById_shouldReturnNews_whenNewsExists() {
    News news = new News();
    news.setNewsId(1L);
    news.setTitle("Test News");

    org.mockito.Mockito.when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

    Optional<News> result = newsRepository.findById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getTitle()).isEqualTo("Test News");
  }

  @Test
  void findById_shouldReturnEmpty_whenNewsDoesNotExist() {
    org.mockito.Mockito.when(newsRepository.findById(9999L)).thenReturn(Optional.empty());

    Optional<News> result = newsRepository.findById(9999L);

    assertThat(result).isEmpty();
  }

  @Test
  void findAll_shouldReturnPagedResults() {
    News news = new News();
    news.setNewsId(1L);
    news.setTitle("Test News");
    Page<News> page = new PageImpl<>(List.of(news), Pageable.unpaged(), 1);

    org.mockito.Mockito.when(newsRepository.findAll(org.mockito.ArgumentMatchers.<Pageable>any()))
        .thenReturn(page);

    Page<News> result = newsRepository.findAll(Pageable.unpaged());

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }
}
