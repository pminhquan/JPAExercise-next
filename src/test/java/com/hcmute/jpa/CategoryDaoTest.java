package com.hcmute.jpa;

import com.hcmute.jpa.dao.CategoryDao;
import com.hcmute.jpa.dao.ICategoryDao;
import com.hcmute.jpa.entity.Category;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testFindByName() {
        ICategoryDao categoryDao = new CategoryDao();
        Category found = categoryDao.findByName("NonExistentCategoryName_12345");
        assertNull(found);

        Category empty = categoryDao.findByName("");
        assertNull(empty);

        Category nullCat = categoryDao.findByName(null);
        assertNull(nullCat);
    }

    @Test
    public void testFindByNamePositiveCaseInsensitiveAndWhitespace() {
        ICategoryDao categoryDao = new CategoryDao();

        // 1. Verify against existing seeded categories if present in database
        List<Category> categories = categoryDao.findAll();
        if (!categories.isEmpty()) {
            Category seeded = categories.get(0);
            String seededName = seeded.getCategoryname();
            Category foundSeededLower = categoryDao.findByName("   " + seededName.toLowerCase() + "   ");
            assertNotNull(foundSeededLower, "Should find seeded category with lowercase and whitespace");
            assertEquals(seeded.getCategoryid(), foundSeededLower.getCategoryid());
            assertEquals(seeded.getCategoryname(), foundSeededLower.getCategoryname());

            Category foundSeededUpper = categoryDao.findByName(" \t " + seededName.toUpperCase() + " \n ");
            assertNotNull(foundSeededUpper, "Should find seeded category with uppercase and whitespace");
            assertEquals(seeded.getCategoryid(), foundSeededUpper.getCategoryid());
        }

        // 2. Self-contained fixture to guarantee positive database-backed regression test
        Category fixture = new Category("Regression Probe Category", "probe.jpg", 1);
        categoryDao.insert(fixture);
        try {
            int fixtureId = fixture.getCategoryid();
            assertTrue(fixtureId > 0, "Fixture should have been assigned an ID");

            // Search using lowercase with surrounding whitespace
            Category foundLower = categoryDao.findByName("   regression probe category   ");
            assertNotNull(foundLower, "Should find category with lowercase and surrounding whitespace");
            assertEquals(fixtureId, foundLower.getCategoryid());
            assertEquals("Regression Probe Category", foundLower.getCategoryname());

            // Search using uppercase with surrounding whitespace
            Category foundUpper = categoryDao.findByName(" \t REGRESSION PROBE CATEGORY \n ");
            assertNotNull(foundUpper, "Should find category with uppercase and surrounding whitespace");
            assertEquals(fixtureId, foundUpper.getCategoryid());
            assertEquals("Regression Probe Category", foundUpper.getCategoryname());
        } finally {
            if (fixture.getCategoryid() > 0) {
                categoryDao.delete(fixture.getCategoryid());
            }
        }
    }
}