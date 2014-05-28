package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 1/14/14.
 */

@Entity("MenuCategoryInfo")
class CategoryInfo extends BaseEntity<CategoryInfo>{
    String title;
    String description;
    String categoryId;
    String parentId;
    List<String> imageIds = new ArrayList<String>();
    List<String> mediaIds = new ArrayList<String>();
}
