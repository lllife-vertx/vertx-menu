package com.ko.handler

import com.ko.model.Result
import com.ko.model.UserInfo
import com.ko.utility.HeaderUtility
import com.ko.utility.StaticLogger
import groovy.json.JsonSlurper
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 1/29/14.
 */
class UserHandler implements  HandlerPrototype{

    Handler<HttpServerRequest> $login() {
       return new Handler<HttpServerRequest>() {

           def logger = StaticLogger.logger()

           @Override
           void handle(HttpServerRequest request) {

               HeaderUtility.allowOrigin(request)

               request.bodyHandler(new Handler<Buffer>() {
                   @Override
                   void handle(Buffer buffer) {
                       def body = buffer.getString(0, buffer.length())
                       def obj = new JsonSlurper().parseText(body)

                       def user = obj.user
                       def pass = obj.password
                       def hash = UserInfo.hash(pass)

                       logger.info("Hash: " + hash)

                       // create query condition
                       def map = new HashMap()
                       map.put("user", user)
                       map.put("password", hash)

                       // query by user && password
                       def rs = UserInfo.$queryBy(UserInfo.class, map)
                       def returnMessage = new Result()


                       // return message
                       if(rs != null){
                           logger.info("== Login Success ==")
                           logger.info("User: " + rs)
                           returnMessage.success =  true;
                       }else {
                           logger.error("== Login Failed ==")
                           logger.error("User: " + user)
                       }

                       def returnString  = returnMessage.toString()
                       request.response().end(returnString)
                   }
               })
           }
       }
    }

    Handler<HttpServerRequest> $logout() {
       return new Handler<HttpServerRequest>() {
           @Override
           void handle(HttpServerRequest request) {

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
