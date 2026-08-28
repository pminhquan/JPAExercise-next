package com.hcmute.jpa.dao;

import com.hcmute.jpa.entity.Product;
import java.util.List;

public interface IProductDao {

    void create(Product product);

    Product findById(int id);

    List<Product> findAll();

    void update(Product product);

    void delete(int id);

    List<Product> findNewest(int limit);

    List<Product> findPage(int offset, int limit);

    long countAll();
}
