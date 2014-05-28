package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 1/14/14.
 */
@Entity("MenuColorInfo")
class ColorInfo extends BaseEntity<ColorInfo>{
    String code;
    String name;
}
