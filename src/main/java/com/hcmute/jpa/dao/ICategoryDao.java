package com.hcmute.jpa.dao;

import com.hcmute.jpa.entity.Category;

import java.util.List;

public interface ICategoryDao {

    void insert(Category category);

    void update(Category category);

    boolean delete(int id);

    Category findById(int id);

    List<Category> findAll();

    boolean isCategoryInUse(int categoryId);

    Category findByName(String name);
}