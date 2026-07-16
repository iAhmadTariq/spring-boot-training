package com.redmath.training.news.service;

import com.redmath.training.news.model.News;
import com.redmath.training.news.repository.NewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        news.setReportedBy(auth.getName());
        return newsRepository.save(news);
    }

    @Transactional
    public News update(Long newsId, News news) {
        News existingNews = newsRepository.findById(newsId)
                .orElseThrow(() -> new NoSuchElementException("News not found with ID: " + newsId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isOwner = existingNews.getReportedBy().equals(auth.getName());
        boolean isEditor = hasAnyRole(auth, "ROLE_editor");

        if (isOwner || isEditor) {
            existingNews.setTitle(news.getTitle());
            existingNews.setDescription(news.getDescription());

            return newsRepository.save(existingNews);
        } else {
            throw new AccessDeniedException("You do not have permission to update this news article.");
        }
    }

    @Transactional
    public News partialUpdate(Long newsId, News news){
        News existingNews = newsRepository.findById(newsId)
                .orElseThrow(()-> new NoSuchElementException("News not found with ID: " + newsId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isOwner = existingNews.getReportedBy().equals(auth.getName());
        boolean isEditor = hasAnyRole(auth, "ROLE_editor");

        if(isOwner || isEditor){
            if (news.getTitle() != null) {
                existingNews.setTitle(news.getTitle());
            }
            if (news.getDescription() != null) {
                existingNews.setDescription(news.getDescription());
            }
            if (news.getReportedBy() != null) {
                existingNews.setReportedBy(news.getReportedBy());
            }
            if (news.getReportedAt() != null) {
                existingNews.setReportedAt(news.getReportedAt());
            }

            newsRepository.save(existingNews);
            return existingNews;

        }else {
            throw new AccessDeniedException("You do not have permission to update this news article.");
        }
    }

    public void delete(Long newsId){
        newsRepository.deleteById(newsId);
    }

    private boolean hasAnyRole(Authentication auth, String role){
        AtomicBoolean result = new AtomicBoolean(false);
        auth.getAuthorities().forEach(value -> {
            if(value.getAuthority().equals(role)){
                result.set(true);
            }
        });
        return result.get();
    }

}
