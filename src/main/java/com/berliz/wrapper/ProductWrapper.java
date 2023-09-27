package com.berliz.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ProductWrapper {

    private Integer id;
    private String uuid;
    private String name;
    private String description;
    private double price;
    private Integer quantity;
    private String photo;
    private String sex;
    private Integer brandId;
    private String brandName;
    private Integer storeId;
    private String storeName;
    private Integer categoryId;
    private String categoryName;
    private Integer tagId;
    private String tagName;
    private Date date;
    private Date lastUpdate;
    private String status;

    public ProductWrapper(Integer id, String uuid, String name, String description,
                          double price, Integer quantity, String photo, String sex,
                          Integer brandId, String brandName, Integer storeId,
                          String storeName, Integer categoryId, String categoryName,
                          Integer tagId, String tagName, Date date, Date lastUpdate,
                          String status) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.photo = photo;
        this.sex = sex;
        this.brandId = brandId;
        this.brandName = brandName;
        this.storeId = storeId;
        this.storeName = storeName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tagId = tagId;
        this.tagName = tagName;
        this.date = date;
        this.lastUpdate = lastUpdate;
        this.status = status;
    }
}
