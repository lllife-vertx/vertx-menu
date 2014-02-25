package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.Connector
import com.ko.model.Result
import com.ko.model.TouchInfo
import com.ko.utility.HeaderUtility
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

    private Connector _connector = Connector.getInstance()

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

    private void addToken(TouchInfo info){
        Calendar c = Calendar.getInstance()
        c.setTime(info.touchDate)

        info.year = c.get(Calendar.YEAR)
        info.month = c.get(Calendar.MONTH)
        info.date = c.get(Calendar.DAY_OF_MONTH)
        info.hour = c.get(Calendar.HOUR_OF_DAY)
        info.minute = c.get(Calendar.MINUTE)
        info.second = c.get(Calendar.MINUTE)
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

                // Connect touch service
                def touchUri = Settings.getTouchUri()
                def touchPort = Settings.getTouchPort()
                def touchHost = Settings.getTouchHost()

                def client = vertx.createHttpClient()
                client.setPort(touchPort)
                client.setHost(touchHost)
                client.setKeepAlive(false)

                def post = client.post("/$touchUri/query", new Handler<HttpClientResponse>() {

                    @Override
                    void handle(HttpClientResponse httpClientResponse) {

                        httpClientResponse.bodyHandler(new Handler<Buffer>() {
                            @Override
                            void handle(Buffer buffer) {
                                def body = buffer.getString(0, buffer.length())

                                _logger.info("<<Response Body>>")
                                _logger.info(body.length());

                            }
                        })

                        httpClientResponse.dataHandler(new Handler<Buffer>(){
                            @Override
                            void handle(Buffer buffer) {
                                def data = buffer.getString(0, buffer.length())

                                _logger.info("<<Reponse Data>>")
                                _logger.info(data.length())

                                List<HashMap> touchs = new JsonSlurper().parseText(data)
                                touchs.each {
                                    it.remove("_id")
                                    Console.println(it)

                                    def touchString = JsonOutput.toJson(it)

                                    TouchInfo touch = BaseEntity.$fromJson(touchString)
                                    addToken(touch)

                                    touch.$save()
                                }

                                def infos = BaseEntity.$toJson(touchs)

                                def returnString = BaseEntity.$toJson(infos)
                                request.response().end(returnString)
                            }
                        });
                    }
                })

                // Get lastest touch record
                def ds = _connector.getDatastore()
                def touchInfo = ds.find(TouchInfo).order("-createDate").get()
                def lastTouchDate = new Date(10,1,1)

                // Use persist date
                if (touchInfo != null)  lastTouchDate = touchInfo.touchDate

                // Base entity
                def format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                def dateString = format.format(lastTouchDate)
                def requestObject = new HashMap()
                requestObject.touchDate = dateString

                def requestJson = BaseEntity.$toJson(requestObject)

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
