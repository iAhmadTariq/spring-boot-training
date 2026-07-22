package com.redmath.training.news.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsDto {

  private long newsId;
  private String title;
  private String description;
  private String reportedBy;
  private LocalDateTime reportedAt = LocalDateTime.now();

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
