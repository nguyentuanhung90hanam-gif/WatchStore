package com.watchstore.repository;

import com.watchstore.model.Post;
import java.util.List;

public interface PostRepository {
    List<Post> findAll();
    Post findById(int id);
    List<Post> search(String keyword);
    boolean insert(Post post);
    boolean update(Post post);
    boolean delete(int id);
    boolean existsBySlug(String slug, Integer excludeId);
}
