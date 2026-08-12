package com.watchstore.repository;

import com.watchstore.model.Banner;
import java.util.List;

public interface BannerRepository {
    List<Banner> findAll();
    Banner findById(int id);
    List<Banner> search(String keyword);
    boolean insert(Banner banner);
    boolean update(Banner banner);
    boolean delete(int id);
}
