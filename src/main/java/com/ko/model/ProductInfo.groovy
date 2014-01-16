package com.ko.model
/**
 * Created by recovery on 12/29/13.
 */
class ProductInfo extends BaseEntity<ProductInfo> {
    String name;
    String productId;
    String description;

    double primaryPrice;
    double promotionPrice;
    double memberPrice;

    String colorId;

    List<String> imageIds = new ArrayList<String>();
    List<String> mediaIds = new ArrayList<String>();
    List<String> categoryIds = new ArrayList<String>();
    List<String> branchInds = new ArrayList<String>();
}
