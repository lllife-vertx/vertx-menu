package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.Connector
import com.ko.model.Result
import com.ko.model.TouchInfo
import com.ko.utility.HeaderUtility
import com.ko.utility.HttpUtility
import com.ko.utility.Settings
import com.ko.utility.StaticLogger
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.vertx.java.core.Handler
import org.vertx.java.core.Vertx
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpClientResponse
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.logging.Logger

import javax.activation.ActivationDataFlavor
import java.text.SimpleDateFormat

/**
 * Created by recovery on 2/24/14.
 */
class TouchHandler implements HandlerPrototype {


    @Override
    Handler<HttpServerRequest> $all() {
        return new Handler<HttpServerRequest>() {

            void handle(HttpServerRequest request) {
                HeaderUtility.allowOrigin(request)

                List<TouchInfo> infos = TouchInfo.$findAll(TouchInfo)
                def jsonString = BaseEntity.$toJson(infos)
                request.response().end(jsonString)
            }
        }
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
        return new Handler<HttpServerRequest>() {

            @Override
            void handle(HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {
                        def body = buffer.getString(0, buffer.length())

                        TouchInfo info = TouchInfo.$fromJson(body);


                        Result rs = info.$save()
                        rs.data = info;

                        def jsonString = BaseEntity.$toJson(rs);
                        request.response().end(jsonString);
                    }
                })
            }
        }
    }


    @Override
    Handler<HttpServerRequest> $upload() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $remove() {
        return null
    }

    Handler<HttpServerRequest> $sync(Vertx vertx) {

        def _logger = StaticLogger.logger();

        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                // Allow access origin
                HeaderUtility.allowOrigin(request)

                def util = new HttpUtility(HttpUtility.Endpoint.Touchs, vertx)
                def client = util.createClient()
                def query = util.getQueryUrl()

                def post = client.post(query, new Handler<HttpClientResponse>() {

                    @Override
                    void handle(HttpClientResponse httpClientResponse) {

                        def message = "";

                        httpClientResponse.dataHandler(new Handler<Buffer>() {
                            @Override

                            void handle(Buffer buffer) {
                                def data = buffer.getString(0, buffer.length())

                                _logger.info("<<Reponse Data>>")
                                _logger.info(data)
                                _logger.info(data.length())

                                message += data;
                            }
                        });

                        httpClientResponse.endHandler(new Handler<Void>() {
                            @Override
                            void handle(Void aVoid) {

                                def touchs = util.processTouchObject(message);

                                // return all processed info... to caller
                                def infos = BaseEntity.$toJson(touchs)
                                request.response().end(infos)
                            }
                        })
                    }
                })

                def requestJson = util.createTouchRequestString()

                // Send request
                _logger.info("<< Request Touch Service >>")
                _logger.info(requestJson)

                post.headers().add("Content-Length", requestJson.getBytes().length.toString())
                post.headers().add("Content-Type", "application/json")
                post.write(requestJson)
                post.end()
            }
        }
    }
}
