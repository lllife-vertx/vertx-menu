package com.ko.model
/**
 * Created by recovery on 12/29/13.
 */
class ProductInfo extends BaseEntity<ProductInfo> {

    String name;
    String description;
    String colorId;

    List<String> imageIds = new ArrayList<String>();
    List<String> mediaIds = new ArrayList<String>();
    List<String> categoryIds = new ArrayList<String>();
    List<String> branchInds = new ArrayList<String>();
}
