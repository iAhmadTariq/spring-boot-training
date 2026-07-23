package com.redmath.training.news.service;

import com.redmath.training.news.model.News;
import com.redmath.training.news.model.NewsDto;
import com.redmath.training.news.repository.NewsRepository;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsService {

  private final NewsRepository newsRepository;

  public NewsService(NewsRepository newsRepository) {
    this.newsRepository = newsRepository;
  }

  public Page<News> findAll(int page, int size, String sortBy, String direction) {
    Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    return newsRepository.findAll(pageable);
  }

  public News findById(Long newsId) {
    return newsRepository.findById(newsId)
        .orElseThrow(() -> new NoSuchElementException("News not found: " + newsId));
  }

  public NewsDto create(NewsDto newsDto) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new AccessDeniedException("Authentication required");
    }
    News news = new News();
    news.setTitle(newsDto.getTitle());
    news.setDescription(newsDto.getDescription());
    news.setReportedBy(auth.getName());
    news.setReportedAt(LocalDateTime.now());

    return NewsDto.from(newsRepository.save(news));
  }

  @Transactional
  public NewsDto update(Long newsId, NewsDto newsDto) {
    News existingNews = newsRepository.findById(newsId)
        .orElseThrow(() -> new NoSuchElementException("News not found with ID: " + newsId));

    authorizeOwnerOrEditor(existingNews);

    existingNews.setTitle(newsDto.getTitle());
    existingNews.setDescription(newsDto.getDescription());

    return NewsDto.from(newsRepository.save(existingNews));
  }

  @Transactional
  public NewsDto partialUpdate(Long newsId, NewsDto newsDto) {
    News existingNews = newsRepository.findById(newsId)
        .orElseThrow(() -> new NoSuchElementException("News not found with ID: " + newsId));

    authorizeOwnerOrEditor(existingNews);

    if (newsDto.getTitle() != null) {
      existingNews.setTitle(newsDto.getTitle());
    }
    if (newsDto.getDescription() != null) {
      existingNews.setDescription(newsDto.getDescription());
    }

    return NewsDto.from(newsRepository.save(existingNews));
  }

  public void delete(Long newsId) throws Exception {
    Optional<News> existingNews = newsRepository.findById(newsId);
    if (existingNews.isEmpty()) {
      throw new Exception("News not found with ID: " + newsId);
    }
    newsRepository.delete(existingNews.get());
  }

  private void authorizeOwnerOrEditor(News existingNews) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new AccessDeniedException("Authentication required");
    }
    boolean isOwner = existingNews.getReportedBy().equals(auth.getName());
    boolean isEditor = hasRole(auth, "editor");

    if (!isOwner && !isEditor) {
      throw new AccessDeniedException("You do not have permission to update this news article.");
    }
  }

  private boolean hasRole(Authentication auth, String role) {
    return auth.getAuthorities().stream()
        .anyMatch(authority -> role.equals(authority.getAuthority()));
  }

}