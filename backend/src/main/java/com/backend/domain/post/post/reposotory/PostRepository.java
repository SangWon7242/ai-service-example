package com.backend.domain.post.post.reposotory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.post.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
