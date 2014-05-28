package com.ko.model

import com.google.code.morphia.annotations.Entity

import java.security.MessageDigest

/**
 * Created by recovery on 1/29/14.
 */
@Entity("MenuUserInfo")
class UserInfo extends BaseEntity<com.ko.model.UserInfo>{
    String user;
    String password;
    String email;
    String firstName;
    String lastName;

    public static String hash(String password){
        def sha256Hash = MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8")).encodeBase64().toString()
        return sha256Hash
    }
}
