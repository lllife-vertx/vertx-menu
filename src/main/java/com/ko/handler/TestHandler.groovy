package com.ko.handler

import com.ko.model.CategoryInfo
import org.vertx.java.core.Handler
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 1/15/14.
 */
class TestHandler {
    Handler<HttpServerRequest> $create(){
        return new Handler<HttpServerRequest>() {
            // @/test/:name
            @Override
            void handle(HttpServerRequest request) {
                def name = request.params().get("name")
                if(name == "category"){
                    def cat = new CategoryInfo(
                            title: "Test Title",
                            description: "Test Description"
                    )
                    def rs = cat.$save()
                    def json = rs.toString()
                    request.response().end(json)
                }
            }
        }
    }
}
