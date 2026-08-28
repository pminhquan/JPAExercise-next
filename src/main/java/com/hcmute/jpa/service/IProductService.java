package com.hcmute.jpa.service;

import com.hcmute.jpa.entity.Product;
import java.util.List;

public interface IProductService {

    void createProduct(Product product);

    Product getProductById(int id);

    List<Product> getAllProducts();

    void updateProduct(Product product);

    void deleteProduct(int id);

    List<Product> getNewestProducts(int limit);

    List<Product> getProductsPage(int offset, int limit);

    long countAllProducts();
}
