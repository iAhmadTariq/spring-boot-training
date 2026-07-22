package com.redmath.training.news.controller;

import com.redmath.training.news.model.News;
import com.redmath.training.news.model.NewsDto;
import com.redmath.training.news.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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

  // Defense-in-depth: even though we now bind to NewsDto (not the News
  // entity) for POST/PUT/PATCH, explicitly whitelist which fields Spring's
  // data binder is allowed to populate. This guards against mass assignment
  // even if a request body type is ever changed back to an entity, or if a
  // new entity-bound endpoint is added later without this in mind.
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.setAllowedFields("title", "description");
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
  public ResponseEntity<News> create(@Valid @RequestBody NewsDto newsDto) {
    News savedNews = newsService.create(newsDto);
    return new ResponseEntity<>(savedNews, HttpStatus.CREATED);
  }

  @PutMapping("/{newsId}")
  public ResponseEntity<News> update(@PathVariable Long newsId, @Valid @RequestBody NewsDto newsDto) {
    News updatedNews = newsService.update(newsId, newsDto);
    return new ResponseEntity<>(updatedNews, HttpStatus.OK);
  }

  @PatchMapping("/{newsId}")
  public ResponseEntity<News> partialUpdate(@PathVariable Long newsId, @RequestBody NewsDto newsDto) {
    News updatedNews = newsService.partialUpdate(newsId, newsDto);
    return new ResponseEntity<>(updatedNews, HttpStatus.OK);
  }

  @DeleteMapping("/{newsId}")
  public ResponseEntity<Void> delete(@PathVariable Long newsId) {
    newsService.delete(newsId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}