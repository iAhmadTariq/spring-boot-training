package com.redmath.training.news.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long newsId;

    @NotBlank(message = "Title cannot be empty")
    @Size(min=5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @Size(max=1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String reportedBy;

    @PastOrPresent(message = "Reported date cannot be in future")
    private LocalDateTime reportedAt = LocalDateTime.now();
}
