package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 2/24/14.
 */
@Entity("MenuTouchInfo")
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

@Entity("MenuRIPInfo")
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

@Entity("MenuSonicInfo")
class SonicInfo extends BaseEntity<SonicInfo> {
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
