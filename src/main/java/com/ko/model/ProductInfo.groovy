package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 12/29/13.
 */
@Entity("MenuProductInfo")
class ProductInfo extends BaseEntity<ProductInfo> {
    String name;
    String productId;
    String description;

    String brand;
    String model;

    boolean highlight;
    boolean promotion;

    double primaryPrice;
    double promotionPrice;
    double memberPrice;

    String colorId;

    List<String> imageIds = new ArrayList<String>();
    List<String> mediaIds = new ArrayList<String>();
    List<String> categoryIds = new ArrayList<String>();
    List<String> branchIds = new ArrayList<String>();
}
