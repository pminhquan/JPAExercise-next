package com.hcmute.jpa.service;

import com.hcmute.jpa.entity.Category;

import java.util.List;

public interface ICategoryService {

    void insert(Category category);

    void update(Category category);

    boolean delete(int id);

    Category findById(int id);

    List<Category> findAll();

    boolean isCategoryInUse(int categoryId);
}