package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 2/21/14.
 */
@Entity(value="MenuDeviceInfo")
class DeviceInfo extends BaseEntity<DeviceInfo> {
    String deviceId;
    String serialNumber;
}
