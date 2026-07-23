package com.redmath.training.news.controller;

import com.redmath.training.news.model.News;
import com.redmath.training.news.model.NewsDto;
import com.redmath.training.news.service.NewsService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

  private final NewsService newsService;

  public NewsController(NewsService newsService) {
    this.newsService = newsService;
  }


  @GetMapping
  public ResponseEntity<Map<String, Object>> findAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "reportedAt") String sortBy,
      @RequestParam(defaultValue = "desc") String direction
  ) {
    Page<News> newsPage = newsService.findAll(page, size, sortBy, direction);
    Map<String, Object> response = new HashMap<>();

    response.put("content", newsPage.getContent().stream()
        .map(NewsDto::from)
        .toList());
    response.put("currentPage", newsPage.getNumber());
    response.put("totalItems", newsPage.getTotalElements());
    response.put("totalPages", newsPage.getTotalPages());
    response.put("isLast", newsPage.isLast());
    response.put("size", newsPage.getSize());

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/{newsId}")
  public NewsDto findById(@PathVariable Long newsId) {
    return NewsDto.from(newsService.findById(newsId));
  }

  @PostMapping
  public ResponseEntity<NewsDto> create(@Valid @RequestBody NewsDto newsDto) {
    NewsDto savedNews = newsService.create(newsDto);
    return new ResponseEntity<>(savedNews, HttpStatus.CREATED);
  }

  @PutMapping("/{newsId}")
  public ResponseEntity<NewsDto> update(@PathVariable Long newsId,
      @Valid @RequestBody NewsDto newsDto) {
    NewsDto updatedNews = newsService.update(newsId, newsDto);
    return new ResponseEntity<>(updatedNews, HttpStatus.OK);
  }

  @PatchMapping("/{newsId}")
  public ResponseEntity<NewsDto> partialUpdate(@PathVariable Long newsId,
      @RequestBody NewsDto newsDto) {
    NewsDto updatedNews = newsService.partialUpdate(newsId, newsDto);
    return new ResponseEntity<>(updatedNews, HttpStatus.OK);
  }

  @DeleteMapping("/{newsId}")
  public ResponseEntity<Void> delete(@PathVariable Long newsId) {
    newsService.delete(newsId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}