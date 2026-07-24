package com.redmath.training.news.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.redmath.training.news.model.News;
import com.redmath.training.news.model.NewsDto;
import com.redmath.training.news.repository.NewsRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

  @Mock
  private NewsRepository newsRepository;
  private NewsService newsService;

  @BeforeEach
  void setUp() {
    newsService = new NewsService(newsRepository);
    SecurityContextHolder.clearContext();
  }

  @Test
  void findAll_shouldReturnPage_whenRepositoryReturnsPage() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("newsId").ascending());
    Page<News> newsPage = new PageImpl<>(java.util.List.of(), pageable, 0);
    when(newsRepository.findAll(pageable)).thenReturn(newsPage);

    Page<News> result = newsService.findAll(0, 10, "newsId", "asc");

    assertThat(result).isEqualTo(newsPage);
    verify(newsRepository).findAll(pageable);
  }

  @Test
  void findAll_shouldSortDescending_whenDirectionIsDesc() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("newsId").descending());
    Page<News> newsPage = new PageImpl<>(java.util.List.of(), pageable, 0);
    when(newsRepository.findAll(pageable)).thenReturn(newsPage);

    newsService.findAll(0, 10, "newsId", "desc");

    verify(newsRepository).findAll(pageable);
  }

  @Test
  void findById_shouldReturnNews_whenNewsExists() {
    News news = new News();
    news.setNewsId(1L);
    news.setTitle("Test Title");
    news.setDescription("Test Description");
    news.setReportedBy("reporter1");
    when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

    News result = newsService.findById(1L);

    assertThat(result).isEqualTo(news);
  }

  @Test
  void findById_shouldThrowNoSuchElementException_whenNewsDoesNotExist() {
    when(newsRepository.findById(9999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> newsService.findById(9999L));
  }

  @Test
  void create_shouldReturnCreatedNews_whenUserIsAuthenticated() {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "reporter1", null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);

    News savedNews = new News();
    savedNews.setNewsId(21L);
    savedNews.setTitle("Test Title");
    savedNews.setDescription("Test Description");
    savedNews.setReportedBy("reporter1");
    when(newsRepository.save(any(News.class))).thenReturn(savedNews);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Test Title");
    newsDto.setDescription("Test Description");

    NewsDto result = newsService.create(newsDto);

    assertThat(result.getNewsId()).isEqualTo(21L);
    assertThat(result.getTitle()).isEqualTo("Test Title");
    assertThat(result.getReportedBy()).isEqualTo("reporter1");
  }

  @Test
  void create_shouldThrowAccessDeniedException_whenUserIsNotAuthenticated() {
    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Test Title");
    newsDto.setDescription("Test Description");

    assertThrows(AccessDeniedException.class, () -> newsService.create(newsDto));
  }

  @Test
  void update_shouldReturnUpdatedNews_whenUserIsOwner() {
    News existingNews = new News();
    existingNews.setNewsId(1L);
    existingNews.setTitle("Original Title");
    existingNews.setDescription("Original Description");
    existingNews.setReportedBy("reporter1");
    when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));
    when(newsRepository.save(existingNews)).thenReturn(existingNews);

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "reporter1", null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Updated Title");
    newsDto.setDescription("Updated Description");

    NewsDto result = newsService.update(1L, newsDto);

    assertThat(result.getTitle()).isEqualTo("Updated Title");
    assertThat(result.getDescription()).isEqualTo("Updated Description");
    verify(newsRepository).save(existingNews);
  }

  @Test
  void update_shouldReturnUpdatedNews_whenUserIsEditor() {
    News existingNews = new News();
    existingNews.setNewsId(1L);
    existingNews.setTitle("Original Title");
    existingNews.setDescription("Original Description");
    existingNews.setReportedBy("reporter1");
    when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));
    when(newsRepository.save(existingNews)).thenReturn(existingNews);

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "editor", null,
        java.util.List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("editor")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Editor Updated Title");
    newsDto.setDescription("Editor Updated Description");

    NewsDto result = newsService.update(1L, newsDto);

    assertThat(result.getTitle()).isEqualTo("Editor Updated Title");
    verify(newsRepository).save(existingNews);
  }

  @Test
  void update_shouldThrowAccessDeniedException_whenUserIsNotOwnerNorEditor() {
    News existingNews = new News();
    existingNews.setNewsId(1L);
    existingNews.setTitle("Original Title");
    existingNews.setDescription("Original Description");
    existingNews.setReportedBy("reporter1");
    when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "reporter2", null,
        java.util.List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("reporter")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Hacked Title");
    newsDto.setDescription("Hacked Description");

    assertThrows(AccessDeniedException.class, () -> newsService.update(1L, newsDto));
  }

  @Test
  void update_shouldThrowNoSuchElementException_whenNewsDoesNotExist() {
    when(newsRepository.findById(9999L)).thenReturn(Optional.empty());

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "reporter1", null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Updated Title");
    newsDto.setDescription("Updated Description");

    assertThrows(NoSuchElementException.class, () -> newsService.update(9999L, newsDto));
  }

  @Test
  void partialUpdate_shouldPreserveUnchangedFields_whenUserIsOwner() {
    News existingNews = new News();
    existingNews.setNewsId(2L);
    existingNews.setTitle("Original Title");
    existingNews.setDescription("Original Description");
    existingNews.setReportedBy("reporter2");
    when(newsRepository.findById(2L)).thenReturn(Optional.of(existingNews));
    when(newsRepository.save(existingNews)).thenReturn(existingNews);

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "reporter2", null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("New Title Only");

    NewsDto result = newsService.partialUpdate(2L, newsDto);

    assertThat(result.getTitle()).isEqualTo("New Title Only");
    assertThat(result.getDescription()).isEqualTo("Original Description");
    verify(newsRepository).save(existingNews);
  }

  @Test
  void partialUpdate_shouldThrowAccessDeniedException_whenUserIsNotOwnerNorEditor() {
    News existingNews = new News();
    existingNews.setNewsId(2L);
    existingNews.setReportedBy("reporter2");
    when(newsRepository.findById(2L)).thenReturn(Optional.of(existingNews));

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "reporter1", null,
        java.util.List.of(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("reporter")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Hacked Title");

    assertThrows(AccessDeniedException.class, () -> newsService.partialUpdate(2L, newsDto));
  }

  @Test
  void partialUpdate_shouldThrowNoSuchElementException_whenNewsDoesNotExist() {
    when(newsRepository.findById(9999L)).thenReturn(Optional.empty());

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "reporter1", null, java.util.List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);

    NewsDto newsDto = new NewsDto();
    newsDto.setTitle("Patched Title");

    assertThrows(NoSuchElementException.class, () -> newsService.partialUpdate(9999L, newsDto));
  }

  @Test
  void delete_shouldDeleteNews_whenNewsExists() {
    News existingNews = new News();
    existingNews.setNewsId(1L);
    when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));

    newsService.delete(1L);

    verify(newsRepository).delete(existingNews);
  }

  @Test
  void delete_shouldThrowNoSuchElementException_whenNewsDoesNotExist() {
    when(newsRepository.findById(9999L)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> newsService.delete(9999L));
  }
}
