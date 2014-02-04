package com.ko.model

import groovy.json.JsonOutput

/**
 * Created by recovery on 12/22/13.
 */
public class Result implements Serializable {
    boolean success;
    String message;
    String id;
    Object data;

    def String toString(){
        //def jsonString = JsonOutput.toJson(this)
        //return  jsonString

        BaseEntity.$toJson(this)
    }
}
