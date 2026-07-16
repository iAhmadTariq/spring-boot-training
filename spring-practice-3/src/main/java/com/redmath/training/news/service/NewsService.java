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

        authorizeOwnerOrEditor(existingNews);

        existingNews.setTitle(news.getTitle());
        existingNews.setDescription(news.getDescription());

        return newsRepository.save(existingNews);
    }

    @Transactional
    public News partialUpdate(Long newsId, News news){
        News existingNews = newsRepository.findById(newsId)
                .orElseThrow(()-> new NoSuchElementException("News not found with ID: " + newsId));

        authorizeOwnerOrEditor(existingNews);

        if (news.getTitle() != null) {
            existingNews.setTitle(news.getTitle());
        }
        if (news.getDescription() != null) {
            existingNews.setDescription(news.getDescription());
        }
        if (news.getReportedAt() != null) {
            existingNews.setReportedAt(news.getReportedAt());
        }

        return newsRepository.save(existingNews);
    }

    public void delete(Long newsId){
        News existingNews = newsRepository.findById(newsId)
                .orElseThrow(() -> new NoSuchElementException("News not found with ID: " + newsId));
        newsRepository.delete(existingNews);
    }

    private void authorizeOwnerOrEditor(News existingNews){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = existingNews.getReportedBy().equals(auth.getName());
        boolean isEditor = hasRole(auth, "ROLE_editor");

        if (!isOwner && !isEditor) {
            throw new AccessDeniedException("You do not have permission to update this news article.");
        }
    }

    private boolean hasRole(Authentication auth, String role){
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

}
