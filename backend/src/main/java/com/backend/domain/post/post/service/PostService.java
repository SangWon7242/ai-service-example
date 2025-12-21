package com.backend.domain.post.post.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.backend.domain.post.post.entity.Post;
import com.backend.domain.post.post.reposotory.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;

  public List<Post> getPosts() {
    return postRepository.findAll();
  }
}
