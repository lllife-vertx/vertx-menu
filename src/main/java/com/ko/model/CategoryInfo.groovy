package com.ko.model

/**
 * Created by recovery on 1/14/14.
 */
class CategoryInfo extends BaseEntity<CategoryInfo>{
    String title;
    String description;
    String id;
    String parentId;
    List<String> imageIds = new ArrayList<String>();
}
