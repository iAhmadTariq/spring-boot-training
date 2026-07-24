package com.redmath.training.news.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsDto {

  private long newsId;

  @NotBlank(message = "Title cannot be empty")
  @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
  private String title;

  @Size(max = 1000, message = "Description cannot exceed 1000 characters")
  private String description;

  private String reportedBy;
  private LocalDateTime reportedAt;

  public static NewsDto from(News news) {
    NewsDto newsDto = new NewsDto();
    newsDto.setNewsId(news.getNewsId());
    newsDto.setTitle(news.getTitle());
    newsDto.setDescription(news.getDescription());
    newsDto.setReportedBy(news.getReportedBy());
    newsDto.setReportedAt(news.getReportedAt());
    return newsDto;
  }
}
