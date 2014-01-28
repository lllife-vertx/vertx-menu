package com.ko

import com.ko.router.MainRouter
import com.ko.utility.StaticLogger
import org.vertx.java.platform.Verticle

/**
 * Created by recovery on 12/29/13.
 */
class MainService extends Verticle{

    @Override
    def void start(){
        def log = container.logger();

        StaticLogger.init(log);

        def server = vertx.createHttpServer()
        def hello = new MainRouter();

        server.requestHandler(hello)
        server.listen(8877, "0.0.0.0")


        log.info("== Main Service ==")
        log.info("Start 0.0.0.0 @8877")


        StaticLogger.logger().info("== Static Logger Is Ok ==")
    }
}
