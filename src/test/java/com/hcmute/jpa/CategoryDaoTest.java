package com.hcmute.jpa;

import com.hcmute.jpa.dao.CategoryDao;
import com.hcmute.jpa.dao.ICategoryDao;
import com.hcmute.jpa.entity.Category;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CategoryDaoTest {

    @Test
    public void testFindAll() {

        ICategoryDao categoryDao = new CategoryDao();

        List<Category> categories =
                categoryDao.findAll();

        System.out.println("============================");

        for (Category category : categories) {
            System.out.println(category);
        }

        System.out.println("============================");
    }
}