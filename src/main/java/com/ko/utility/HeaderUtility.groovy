package com.ko.utility

import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 1/15/14.
 */
class HeaderUtility {
    static void allowOrigin(HttpServerRequest request){
        //TODO: externalize the Allow-Origin
        request.response().putHeader("Access-Control-Allow-Origin", "*")
        request.response().putHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD")
//        request.response().putHeader("Access-Control-Allow.putHeaderPINGOTHER, Origin, X-Requested-With, Content-Type, Accept")
//        request.response().putHeader("Access-Control-Max-Age", "1728000")
    }
}
