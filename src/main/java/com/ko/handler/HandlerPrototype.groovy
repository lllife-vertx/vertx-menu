package com.ko.handler

import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 12/29/13.
 */
interface  HandlerPrototype<T> {
    Handler<HttpServerRequest> $all();
    Handler<HttpServerRequest> $byId();
    Handler<HttpServerRequest> $byExample();
    Handler<HttpServerRequest> $add();
    Handler<HttpServerRequest> $upload();
}