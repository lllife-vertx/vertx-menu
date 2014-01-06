package com.ko.router

import com.ko.handler.ProductHandler
import com.ko.model.Connector
import org.vertx.java.core.Handler
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.http.RouteMatcher

/**
 * Created by recovery on 12/29/13.
 */
class MainRouter extends RouteMatcher {

    private def _connector = new Connector()

    MainRouter() {
        super();

        // product
        def product = new ProductHandler()
        this.get("/product", product.$all())
        this.get("/product/:id", product.$byId())
        this.post("/product", product.$add())
        this.put("/product", product.$add())
        this.post("/product/query", product.$byExample())
        //this.post("/test", null)

        this.noMatch(new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.response().statusCode = 404
                request.response().end()
            }
        })
    }
}
