package com.redmath.training.news.repository;

import com.redmath.training.news.model.News;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

  @Query("SELECT n FROM News n WHERE n.reportedAt > :dateTime")
  List<News> findNewsAfterThisDate(@Param("dateTime") LocalDateTime dateTime);
}
