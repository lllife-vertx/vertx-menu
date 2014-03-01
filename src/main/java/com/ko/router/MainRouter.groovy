package com.ko.router

import com.ko.handler.BranchHandler
import com.ko.handler.CategoryHander
import com.ko.handler.DeviceHandler
import com.ko.handler.ImageHandler
import com.ko.handler.MediaHandler
import com.ko.handler.PIRHandler
import com.ko.handler.ProductHandler
import com.ko.handler.ReportHandler
import com.ko.handler.TestHandler
import com.ko.handler.TouchHandler
import com.ko.handler.UserHandler
import org.vertx.java.core.Handler
import org.vertx.java.core.Vertx
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.http.RouteMatcher

/**
 * Created by recovery on 12/29/13.
 */
class MainRouter extends RouteMatcher {

    private Vertx _vertx = null

    def registerProduct() {
        def product = new ProductHandler()

        this.get("/product", product.$all())
        this.get("/product/:id", product.$byId())
        this.post("/product", product.$add())
        this.put("/product", product.$add())
        this.post("/product/query", product.$byExample())
        this.post("/product/up", product.$upload())
    }

    def registerCategory(){
        def category = new CategoryHander()

        this.get("/category", category.$all())
        this.post("/category", category.$add())
        this.post("/category/query", category.$byExample())
    }

    def registerVideo(){
        def video = new MediaHandler()

        this.post("/video/upload", video.$upload())
        this.get("/video/url/:id", video.$byId())
        this.get("/video/:id", video.$byId())
    }

    def registerImage(){
        def image = new ImageHandler()

        this.post("/image/delete/:id", image.$remove());
        // this.post("/delete/id", image.$remove())
        this.post("/image", image.$add())
        this.get("/image/:id", image.$byId())
        this.post("/image/upload", image.$upload())
        this.get("/image/url/:id", image.$byId())
        this.get("/image/thumbnail/:id", image.$byId())
    }

    def registerUser(){

        def user = new UserHandler()
        this.post("/user/login", user.$login());

        def test = new TestHandler()
        this.get("/test/:name", test.$create())
    }

    def registerBranch(){
        def branch = new BranchHandler()
        this.get("/branch", branch.$all())
        this.post("/branch", branch.$add())
    }

    def registerDevice(){
        def device = new DeviceHandler()
        this.get("/device", device.$all())
        this.post("/device", device.$add())
    }

    def registerTouch(){
        def touch = new TouchHandler()
        this.get("/touch", touch.$all())
        this.post("/touch", touch.$add())
        this.get("/touch/sync", touch.$sync(_vertx))
    }

    def registerReport(){
        def report = new ReportHandler()
        this.post("/report/compare/chart", report.$queryReport());
        this.post("/report/compare/summary", report.$querySummary())
    }

    def registerPIR(){
        def pir = new PIRHandler()
        this.get("/pir/sync", pir.$sync(_vertx))
    }

    private MainRouter(Vertx vertx) {
        super();

        _vertx = vertx
        this.registerProduct()
        this.registerCategory()
        this.registerVideo()
        this.registerImage()
        this.registerUser()
        this.registerBranch()
        this.registerDevice()
        this.registerTouch()
        this.registerPIR();

        this.registerReport()

        this.noMatch(new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.response().statusCode = 404
                request.response().end()
            }
        })
    }

    public Vertx GetVertx() {
        return _vertx
    }

    public static MainRouter getInstance(Vertx vertx){
        return new MainRouter(vertx)
    }
}
