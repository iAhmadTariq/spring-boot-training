package com.redmath.training.news.service;

import com.redmath.training.news.model.News;
import com.redmath.training.news.repository.NewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository){
        this.newsRepository = newsRepository;
    }

    public Page<News> findAll(int page, int size, String sortBy, String direction){
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page,size,sort);
        return newsRepository.findAll(pageable);
    }

    public News findById(Long newsId){
        return newsRepository.findById(newsId).orElseThrow(()-> new NoSuchElementException("News not found: " + newsId));
    }

    public News create(News news){
        return newsRepository.save(news);
    }

    public News update(Long newsId, News news){
        if (!newsRepository.existsById(newsId)) {
            throw new NoSuchElementException("News not found: " + newsId);
        }
        news.setNewsId(newsId);
        return newsRepository.save(news);
    }

    public News partialUpdate(Long newsId, News news){
        News existingNews = findById(newsId);

        if (news.getTitle() != null) {
            existingNews.setTitle(news.getTitle());
        }
        if (news.getDescription() != null) {
            existingNews.setDescription(news.getDescription());
        }
        if (news.getAuthor() != null) {
            existingNews.setAuthor(news.getAuthor());
        }
        if (news.getReportedAt() != null) {
            existingNews.setReportedAt(news.getReportedAt());
        }

        newsRepository.save(existingNews);
        return existingNews;
    }

    public void delete(Long newsId){
        newsRepository.deleteById(newsId);
    }
}
