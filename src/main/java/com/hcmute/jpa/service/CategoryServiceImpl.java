package com.hcmute.jpa.service;

import com.hcmute.jpa.dao.CategoryDao;
import com.hcmute.jpa.dao.ICategoryDao;
import com.hcmute.jpa.entity.Category;

import java.util.List;

public class CategoryServiceImpl implements ICategoryService {

    private final ICategoryDao categoryDao;

    public CategoryServiceImpl() {
        this.categoryDao = new CategoryDao();
    }

    @Override
    public void insert(Category category) {
        categoryDao.insert(category);
    }

    @Override
    public void update(Category category) {
        categoryDao.update(category);
    }

    @Override
    public boolean delete(int id) {
        return categoryDao.delete(id);
    }

    @Override
    public Category findById(int id) {
        return categoryDao.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryDao.findAll();
    }

    @Override
    public boolean isCategoryInUse(int categoryId) {
        return categoryDao.isCategoryInUse(categoryId);
    }

    @Override
    public Category findByName(String name) {
        return categoryDao.findByName(name);
    }
}