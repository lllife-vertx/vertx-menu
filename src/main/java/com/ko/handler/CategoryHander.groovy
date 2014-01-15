package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.CategoryInfo
import com.ko.model.ProductInfo
import com.ko.utility.HeaderUtility
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 1/15/14.
 */
class CategoryHander implements HandlerPrototype<CategoryInfo> {
    @Override
    Handler<HttpServerRequest> $all() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                def rs = CategoryInfo.$findAll(CategoryInfo.class)
                def json = BaseEntity.$toJson(rs)

                HeaderUtility.allowOrigin(request)
                request.response().end(json)
            }
        }
    }

    @Override
    Handler<HttpServerRequest> $byId() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $byExample() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {
                        // extract json string
                        def json = buffer.getString(0, buffer.length())

                        // create entity object
                        CategoryInfo info = CategoryInfo.$fromJson(json)
                        info._id = info.identifier != null ? new ObjectId(info.identifier) : null

                        // find and return
                        def rs = CategoryInfo.$findAllByExample(info)
                        def jsonResult = BaseEntity.$toJson(rs)

                        request.response().end(jsonResult)
                    }
                })
            }
        }
    }

    @Override
    Handler<HttpServerRequest> $add() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                Console.println("Request ...");

                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {

                        // extract json string
                        String json = buffer.getString(0, buffer.length())


                        Console.println(json)
                        Console.println("=====================================")

                        // create object from json
                        //def info = ProductInfo.$fromJson(json, ProductInfo.getClass())
                        CategoryInfo info = ProductInfo.$fromJson(json)
                        info._id = info.identifier != null ? new ObjectId(info.identifier) : null


                        // save and return result
                        def rs = info.$save()
                        rs.data = info

                        HeaderUtility.allowOrigin(request)
                        request.response().end(rs.toString())
                    }
                })
            }
        }
    }

    @Override
    Handler<HttpServerRequest> $upload() {
        return null
    }
}
