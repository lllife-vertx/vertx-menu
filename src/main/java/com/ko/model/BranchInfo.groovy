package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 1/15/14.
 */
@Entity(value = "MenuBranchInfo")
class BranchInfo  extends BaseEntity<BranchInfo>{
    String branchId;
    String name;
    String province;
    String district;
    String phone;
    String description;
    List<String> deviceIds = new ArrayList<String>();
}