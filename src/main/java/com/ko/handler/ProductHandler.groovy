package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.ImageInfo
import com.ko.model.ProductInfo
import com.ko.model.Result
import com.ko.utility.HeaderUtility
import groovy.json.JsonOutput
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerFileUpload
import org.vertx.java.core.http.HttpServerRequest

import java.lang.invoke.ConstantCallSite

/**
 * Created by recovery on 12/29/13.
 */
class ProductHandler implements HandlerPrototype<ProductInfo> {
    @Override
    Handler<HttpServerRequest> $all() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                HeaderUtility.allowOrigin(request)

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

                HeaderUtility.allowOrigin(request)

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

                HeaderUtility.allowOrigin(request)

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
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {

                        HeaderUtility.allowOrigin(request)

                        // extract json string
                        String json = buffer.getString(0, buffer.length())

                        // create object from json
                        ProductInfo info = ProductInfo.$fromJson(json)
                        info._id = info.identifier != null ? new ObjectId(info.identifier) : null

                        // save and return result
                        def rs = info.$save()
                        rs.data = info

                        // Return
                        def returnJson = rs.toString()
                        request.response().end(returnJson)
                    }
                })
            }
        }
    }

    @Override
    Handler<HttpServerRequest> $upload() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                // Expect multipart/formdata
                request.expectMultiPart(true)

                // Register upload hander
                request.uploadHandler(new Handler<HttpServerFileUpload>() {
                    @Override
                    void handle(HttpServerFileUpload upload) {

                        def currentFilePath = ""

                        // End request hander.
                        upload.endHandler(new Handler<Void>() {
                            @Override
                            void handle(Void aVoid) {     // Save meta data

                                String.metaClass.decodeUrl = { java.net.URLDecoder.decode(delegate) }

                                def jsonData = upload.req.decoder.bodyMapHttpData["data"][0]["value"]
                                def jsonString = jsonData.decodeUrl()

                                ImageInfo info = ImageInfo.$fromJson(jsonString)
                                info.path = currentFilePath

                                def rs = info.$save()
                                rs.data = rs;

                                request.response().end(rs.toString())
                            }
                        })

                        // Save file
                        def path = "/home/recovery/sources/emenu/VertxService/upload"
                        def fullPath = new File(path, upload.filename()).getPath()
                        upload.streamToFileSystem(fullPath)
                        currentFilePath = fullPath
                    }
                })

            }
        }
    }

    @Override
    Handler<HttpServerRequest> $remove() {
        return null
    }
}