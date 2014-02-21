package com.ko.model

/**
 * Created by recovery on 1/15/14.
 */
class BranchInfo  extends BaseEntity<BranchInfo>{
    String branchId;
    String name;
    String province;
    String district;
    String phone;
    String description;
    List<String> deviceIds = new ArrayList<String>();
}