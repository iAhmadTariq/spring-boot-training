package com.redmath.training.news.controller;

import com.redmath.training.news.model.News;
import com.redmath.training.news.service.NewsService;
import jakarta.validation.Valid;
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
  public ResponseEntity<Page<News>> findAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "reportedAt") String sortBy,
      @RequestParam(defaultValue = "desc") String direction
  ) {
    Page<News> newsPage = newsService.findAll(page, size, sortBy, direction);
    return new ResponseEntity<>(newsPage, HttpStatus.OK);
  }


  @GetMapping("/{newsId}")
  public News findById(@PathVariable Long newsId) {
    return newsService.findById(newsId);
  }

  @PostMapping
  public ResponseEntity<News> create(@Valid @RequestBody News news) {
    News savedNews = newsService.create(news);
    return new ResponseEntity<>(savedNews, HttpStatus.CREATED);
  }

  @PutMapping("/{newsId}")
  public ResponseEntity<News> update(@PathVariable Long newsId, @Valid @RequestBody News news) {
    News updatedNews = newsService.update(newsId, news);
    return new ResponseEntity<>(updatedNews, HttpStatus.OK);
  }

  @PatchMapping("/{newsId}")
  public ResponseEntity<News> partialUpdate(@PathVariable Long newsId, @RequestBody News news) {
    News updatedNews = newsService.partialUpdate(newsId, news);
    return new ResponseEntity<>(updatedNews, HttpStatus.OK);
  }

  @DeleteMapping("/{newsId}")
  public ResponseEntity<Void> delete(@PathVariable Long newsId) {
    newsService.delete(newsId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}