package com.example.oauthserver.common.repository;

import com.example.oauthserver.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Article, Long> {
}