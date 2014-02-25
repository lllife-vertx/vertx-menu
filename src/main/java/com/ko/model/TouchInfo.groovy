package com.ko.model

/**
 * Created by recovery on 2/24/14.
 */
class TouchInfo extends BaseEntity<TouchInfo> {

    Date collectDate;
    Date touchDate;

    String objectType;
    String objectId;
    String deviceId;

    int year;
    int date;
    int month;
    int hour;
    int minute;
    int second;
}
