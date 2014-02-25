package com.ko

import com.ko.handler.SynchronizeHandler
import com.ko.router.MainRouter
import com.ko.utility.StaticLogger
import org.vertx.java.core.http.HttpServer
import org.vertx.java.core.json.JsonArray
import org.vertx.java.platform.Verticle
import org.vertx.java.core.json.JsonObject

/**
 * Created by recovery on 12/29/13.
 */
class MainService extends Verticle{

    @Override
    def void start(){

        // Init global log.
        def log = container.logger();
        StaticLogger.init(log);

        // Register event bus.
        this.$registerEventBus()

        // Register serice
        def server = vertx.createHttpServer()

        def hello = MainRouter.getInstance(this.vertx)
        server.requestHandler(hello)

        // Create socket service.
        this.$createSockJsService(server)

        // Start litening...
        server.listen(8877, "0.0.0.0")

        // Init message...
        log.info("== Main Service ==")
        log.info("== Start 0.0.0.0 @8877")

        // Check global log
        // StaticLogger.logger().info("== Static Logger Is Ok ==")
    }

    def void $registerEventBus(){
        def sync = new SynchronizeHandler(this)
        sync.$register()
    }

    def void $createSockJsService(HttpServer server){
        def config = new JsonObject()
        config.putValue("prefix", "/eventbus")

        def inc = new JsonArray()
        def out = new JsonArray()

        inc.add(new JsonObject())
        out.add(new JsonObject())

        def sockServer = vertx.createSockJSServer(server)
        sockServer.bridge(config, inc, out )
    }
}
