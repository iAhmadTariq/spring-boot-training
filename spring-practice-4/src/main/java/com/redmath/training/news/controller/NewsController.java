package com.redmath.training.news.controller;

import com.redmath.training.news.model.News;
import com.redmath.training.news.model.NewsDto;
import com.redmath.training.news.service.NewsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class NewsController {

  private static final Logger log = LoggerFactory.getLogger(NewsController.class);
  private final NewsService newsService;

  public NewsController(NewsService newsService) {
    this.newsService = newsService;
  }


  @GetMapping
  public ResponseEntity<Map<String, Object>> findAll(
      @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page index must not be negative") int page,
      @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size must not exceed 100") int size,
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
  public ResponseEntity<NewsDto> findById(@PathVariable Long newsId) {
    return ResponseEntity.ok(NewsDto.from(newsService.findById(newsId)));
  }

  @GetMapping("/after")
  public ResponseEntity<List<NewsDto>> findNewsAfterThisDate(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    List<NewsDto> newsDto = newsService.findNewsAfterThisDate(date)
        .stream()
        .map(NewsDto::from)
        .toList();

    return ResponseEntity.ok(newsDto);
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
      @Valid @RequestBody NewsDto newsDto) {
    NewsDto updatedNews = newsService.partialUpdate(newsId, newsDto);
    return new ResponseEntity<>(updatedNews, HttpStatus.OK);
  }

  @DeleteMapping("/{newsId}")
  public ResponseEntity<Void> delete(@PathVariable Long newsId) {
    newsService.delete(newsId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}