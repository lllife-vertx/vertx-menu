package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 12/29/13.
 */
@Entity("MenuMediaInfo")
class MediaInfo extends BaseEntity<MediaInfo> {
    String title;
    String description;
    String type;
    String path;
}
