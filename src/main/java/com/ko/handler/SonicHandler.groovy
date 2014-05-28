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

/**
 * Created by recovery on 5/28/14.
 */
class SonicHandler implements HandlerPrototype<SonicHandler>{

    def logger = StaticLogger.logger();

    Handler<HttpServerRequest> $sync(Vertx vertx) {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                HeaderUtility.allowOrigin(request);

                // Use http utility for all sensor hander.
                def util = new HttpUtility(HttpUtility.Endpoint.Sonics, vertx)
                def client = util.createClient()
                def path = util.getQueryUrl()

                logger.info("Connect: $path");

                // Requst to sonic endpoint.
                def post = client.post(path, new Handler<HttpClientResponse>() {
                    @Override
                    void handle(HttpClientResponse httpClientResponse) {
                        def message = ""
                        httpClientResponse.dataHandler(new Handler<Buffer>(){
                            @Override
                            void handle(Buffer buffer) {
                                def body = buffer.getString(0, buffer.length())
                                message += body;
                            }
                        })

                        httpClientResponse.endHandler(new Handler<Void>() {
                            @Override
                            void handle(Void aVoid) {
                                def object = util.processSonicObject(message)
                                def jsonString = BaseEntity.$toJson(object)
                                request.response().end(jsonString)
                            }
                        })
                    }
                })

                def requestJson = util.createSonicRequestString()

                // Send request
                logger.info("<< Request Sonic Service >>")
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
