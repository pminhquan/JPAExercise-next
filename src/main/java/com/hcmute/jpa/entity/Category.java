package com.hcmute.jpa.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "categories")
@NamedQuery(
        name = "Category.findAll",
        query = "SELECT c FROM Category c ORDER BY c.categoryid DESC"
)
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryId")
    private int categoryid;

    @Column(
            name = "CategoryName",
            nullable = false,
            columnDefinition = "NVARCHAR(100)"
    )
    private String categoryname;

    @Column(
            name = "Images",
            columnDefinition = "NVARCHAR(500)"
    )
    private String images;

    @Column(name = "Status")
    private int status;

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Category() {
    }

    public Category(String categoryname, String images, int status) {
        this.categoryname = categoryname;
        this.images = images;
        this.status = status;
    }

    public Category(int categoryid,
                    String categoryname,
                    String images,
                    int status) {

        this.categoryid = categoryid;
        this.categoryname = categoryname;
        this.images = images;
        this.status = status;
    }

    public int getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(int categoryid) {
        this.categoryid = categoryid;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryid=" + categoryid +
                ", categoryname='" + categoryname + '\'' +
                ", images='" + images + '\'' +
                ", status=" + status +
                '}';
    }
}