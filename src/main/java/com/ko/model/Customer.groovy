package com.ko.model

import com.google.code.morphia.annotations.Entity

/**
 * Created by recovery on 12/21/13.
 */

@Entity("Customers")
class Customer extends BaseEntity<Customer> {

    String name;
    String lastName;
    String email;
    String address;
}
