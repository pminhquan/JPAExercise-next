package com.hcmute.jpa.service;

import com.hcmute.jpa.entity.Category;

import java.util.List;

public interface ICategoryService {

    void insert(Category category);

    void update(Category category);

    void delete(int id);

    Category findById(int id);

    List<Category> findAll();
}