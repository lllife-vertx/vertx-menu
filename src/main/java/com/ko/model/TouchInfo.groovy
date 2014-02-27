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

    String collectId;

    int year;
    int date;
    int month;
    int hour;
    int minute;
    int second;
}


class PIRInfo extends BaseEntity<PIRInfo> {

    Date collectDate;

    Date enterDate;
    Date leaveDate;

    String deviceId;

    String collectId;

    int year;
    int date;
    int month;
    int hour;
    int minute;
    int second;
}

