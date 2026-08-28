package com.hcmute.jpa.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "products")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private int productid;

    @Column(
            name = "ProductName",
            nullable = false,
            columnDefinition = "NVARCHAR(250)"
    )
    private String productname;

    @Column(
            name = "Description",
            columnDefinition = "NVARCHAR(500)"
    )
    private String description;

    @Column(name = "Price")
    private double price;

    @Column(
            name = "Images",
            columnDefinition = "NVARCHAR(500)"
    )
    private String images;

    @Column(name = "Status")
    private int status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    public Product() {
    }

    public Product(String productname, String description, double price, String images, int status, Category category) {
        this.productname = productname;
        this.description = description;
        this.price = price;
        this.images = images;
        this.status = status;
        this.category = category;
    }

    public int getProductid() {
        return productid;
    }

    public void setProductid(int productid) {
        this.productid = productid;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productid=" + productid +
                ", productname='" + productname + '\'' +
                ", price=" + price +
                ", status=" + status +
                '}';
    }
}
