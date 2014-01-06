package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.ProductInfo
import com.ko.model.Result
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 12/29/13.
 */
class ProductHandler implements HandlerPrototype<ProductInfo> {
    @Override
    Handler<HttpServerRequest> $all() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                try {
                    def rs = ProductInfo.$findAll(ProductInfo.class)
                    def jsonString = BaseEntity.$toJson(rs)
                    request.response().end(jsonString)
                } catch (e) {
                    def rs = new Result(message: e.getMessage(), success: false)
                    def json = BaseEntity.$toJson(rs)

                    request.response().statusCode = 500;
                    request.response().end(json)
                }
            }
        }
    }

    @Override
    Handler<HttpServerRequest> $byId() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                def id = request.params().get("id")
                def objId = new ObjectId(id)
                def ex = new ProductInfo(_id: objId)
                def rs = ProductInfo.$findByExample(ex)
                def json = BaseEntity.$toJson(rs)
                request.response().end(json)
            }
        }
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
                        ProductInfo info = ProductInfo.$fromJson(json)
                        info._id = info.identifier != null ? new ObjectId(info.identifier) : null

                        // find and return
                        def rs = ProductInfo.$findAllByExample(info)
                        def jsonResult = ProductInfo.$toJson(rs)
                        request.response().end(jsonResult)
                    }
                })
            }
        }
    }

    @Override
    Handler<HttpServerRequest> $add() {
        return  new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {

                        // extract json string
                        String json = buffer.getString(0, buffer.length())

                        // create object from json
                        ProductInfo info =  ProductInfo.$fromJson(json)
                        info._id = info.identifier != null ? new ObjectId(info.identifier) : null

                        // save and return result
                        def rs = info.$save()
                        request.response().end(rs.toString());
                    }
                })
            }
        }
    }
}
