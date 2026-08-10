package com.watchstore.repository;

import com.watchstore.model.Brand;
import java.util.List;

public interface BrandRepository {

    List<Brand> findAll();

    Brand findById(int id);

    List<Brand> search(String keyword);

    boolean existsByCode(String code, Integer excludeId);

    boolean existsBySlug(String slug, Integer excludeId);

    boolean insert(Brand brand);

    boolean update(Brand brand);

    boolean delete(int id);

}