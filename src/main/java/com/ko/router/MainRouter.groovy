package com.ko.router

import com.ko.handler.CategoryHander
import com.ko.handler.ImageHandler
import com.ko.handler.ProductHandler
import com.ko.handler.TestHandler
import com.ko.handler.UserHandler
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
        this.post("/product/up", product.$upload())

        // category
        def category = new CategoryHander()
        this.get("/category", category.$all())
        this.post("/category", category.$add())
        this.post("/category/query", category.$byExample())


        // image
        def image = new ImageHandler()

        this.post("/image/delete/:id", image.$remove());
//        this.post("/delete/id", image.$remove())
        this.post("/image", image.$add())

        this.get("/image/:id", image.$byId())
        this.post("/image/upload", image.$upload())
        this.get("/image/url/:id", image.$byId())
        this.get("/image/thumbnail/:id", image.$byId())

        //this.delete("/image/:id", image.$remove())

        // user
        def user = new UserHandler()
        this.post("/user/login", user.$login());

        // test
        def test = new TestHandler()
        this.get("/test/:name", test.$create())

        this.noMatch(new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.response().statusCode = 404
                request.response().end()
            }
        })
    }
}
