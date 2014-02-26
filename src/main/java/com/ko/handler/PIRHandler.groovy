package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.utility.HeaderUtility
import com.ko.utility.HttpUtility
import com.ko.utility.StaticLogger
import org.vertx.java.core.Handler
import org.vertx.java.core.Vertx
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpClientResponse
import org.vertx.java.core.http.HttpServerRequest

class PIRHandler  implements HandlerPrototype<com.ko.handler.PIRHandler>{

    Handler<HttpServerRequest> $sync(Vertx vertx){

        def logger = StaticLogger.logger()

        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                HeaderUtility.allowOrigin(request)

                def util = new HttpUtility(HttpUtility.Endpoint.Pirs, vertx)
                def client = util.createClient()
                def path = util.getQueryUrl()

                def post = client.post(path, new Handler<HttpClientResponse>() {
                    @Override
                    void handle(HttpClientResponse httpClientResponse) {
                        httpClientResponse.dataHandler(new Handler<Buffer>(){

                            @Override
                            void handle(Buffer buffer) {

                                def body = buffer.getString(0, buffer.length())
                                def object = util.processPIRObject(body)

                                def jsonString = BaseEntity.$toJson(object)
                                request.response().end(jsonString)
                            }
                        })
                    }
                })

                def requestJson = util.createPIRRequestString()

                // Send request
                logger.info("<< Request PIR Service >>")
                logger.info(requestJson)
                logger.info("<< Path == $path >>")

                post.headers().add("Content-Length", requestJson.getBytes().length.toString())
                post.headers().add("Content-Type", "application/json")
                post.write(requestJson)
                post.end()
            }
        }
    }


    @Override
    Handler<HttpServerRequest> $all() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $byId() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $byExample() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $add() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $upload() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $remove() {
        return null
    }
}
