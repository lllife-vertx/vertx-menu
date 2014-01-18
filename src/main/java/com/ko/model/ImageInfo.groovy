package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 12/29/13.
 */
@Entity("ImageInfo")
class ImageInfo extends BaseEntity<ImageInfo> {
    String title;
    String type;
    String description;
    String path;
}
