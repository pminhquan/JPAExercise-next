package com.hcmute.jpa.service;

import com.hcmute.jpa.dao.ICategoryDao;
import com.hcmute.jpa.dao.CategoryDao;
import com.hcmute.jpa.dao.IProductDao;
import com.hcmute.jpa.dao.ProductDao;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import java.util.List;

public class ProductServiceImpl implements IProductService {

    private final IProductDao productDao;
    private final ICategoryDao categoryDao;

    public ProductServiceImpl() {
        this.productDao = new ProductDao();
        this.categoryDao = new CategoryDao();
    }

    public ProductServiceImpl(IProductDao productDao, ICategoryDao categoryDao) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
    }

    @Override
    public void createProduct(Product product) {
        validateProductForCreate(product);
        productDao.create(product);
    }

    @Override
    public Product getProductById(int id) {
        return productDao.findById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    @Override
    public void updateProduct(Product product) {
        validateProductForUpdate(product);
        productDao.update(product);
    }

    @Override
    public void deleteProduct(int id) {
        productDao.delete(id);
    }

    @Override
    public List<Product> getNewestProducts(int limit) {
        return productDao.findNewest(limit);
    }

    @Override
    public List<Product> getProductsPage(int offset, int limit) {
        return productDao.findPage(offset, limit);
    }

    @Override
    public long countAllProducts() {
        return productDao.countAll();
    }

    private void validateProductForCreate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getProductname() == null || product.getProductname().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (product.getPrice() <= 0 || !Double.isFinite(product.getPrice())) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (product.getPrice() != Math.rint(product.getPrice())) {
            throw new IllegalArgumentException("Price must be a whole number");
        }
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        int categoryId = product.getCategory().getCategoryid();
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category must already exist");
        }
        Category existingCategory = categoryDao.findById(categoryId);
        if (existingCategory == null) {
            throw new IllegalArgumentException("Category does not exist");
        }
        product.setCategory(existingCategory);
    }

    private void validateProductForUpdate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getProductname() == null || product.getProductname().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (product.getPrice() <= 0 || !Double.isFinite(product.getPrice())) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (product.getPrice() != Math.rint(product.getPrice())) {
            throw new IllegalArgumentException("Price must be a whole number");
        }

        Product existingProduct = productDao.findById(product.getProductid());
        if (existingProduct == null) {
            throw new IllegalArgumentException("Product to update does not exist");
        }

        if (product.getCategory() == null) {
            product.setCategory(existingProduct.getCategory());
        } else {
            int categoryId = product.getCategory().getCategoryid();
            if (categoryId <= 0) {
                throw new IllegalArgumentException("Category must already exist");
            }
            Category existingCategory = categoryDao.findById(categoryId);
            if (existingCategory == null) {
                throw new IllegalArgumentException("Category does not exist");
            }
            product.setCategory(existingCategory);
        }
    }
}
