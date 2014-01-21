package com.ko.handler

import com.ko.model.ImageInfo
import com.ko.model.ProductInfo
import com.ko.utility.HeaderUtility
import com.ko.utility.Settings
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.name.Rename
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerFileUpload
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 1/16/14.
 */
class ImageHandler implements HandlerPrototype<com.ko.handler.ImageHandler> {

    private static Logger _logger = LogManager.getLogger(ImageHandler.class)

    @Override
    Handler<HttpServerRequest> $all() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $byId() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                HeaderUtility.allowOrigin(request)

                try {
                    def id = request.params().get("id")

                    def objectId = new ObjectId(id)
                    def image = new ImageInfo(_id: objectId)
                    //def returnImage = ImageInfo.$findByExample(image)
                    ImageInfo returnImage = ImageInfo.$findById(ImageInfo.class, objectId)

                    // return file
                    if (request.uri().contains("url")) {
                        def base = Settings.getUploadPath()
                        def full = new File(base, returnImage.path).getPath()

                       Console.println("Return full: " + full)

                        request.response().sendFile(full)
                    }
                    else if(request.uri().contains("thumbnail")){
                        def base = Settings.getUploadPath()
                        def full = new File(base, returnImage.path).getPath()
                        def thumbnail = full + "_thumbnail.jpg";

                        Console.println("Return thumbnail: " + thumbnail)

                        request.response().sendFile(thumbnail)
                    } else {
                        // return info
                        def jsonString = returnImage.$toJson()
                        request.response().end(jsonString)
                    }
                } catch (Exception ex) {
                    request.response().statusCode = 501
                    request.response().end(ex.getMessage())
                }
            }
        }
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

                        HeaderUtility.allowOrigin(request)

                        // extract json string
                        String json = buffer.getString(0, buffer.length())

                        // create object from json
                        ImageInfo info = ImageInfo.$fromJson(json)
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

                Console.println("Request...")
                Console.println("=======================")

                // Expect multipart/formdata
                request.expectMultiPart(true)

                HeaderUtility.allowOrigin(request)

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
                                def base = Settings.getUploadPath()

                                ImageInfo info = ImageInfo.$fromJson(jsonString)
                                info.path = currentFilePath.replaceAll(base, "")

                                def rs = info.$save()
                                rs.data = info;

                                // create thumbnails
                                def fullPath = currentFilePath
                                def thumbnail = fullPath + "_thumbnail.jpg"
                                Thumbnails.of(new File(fullPath)).size(300, 300).toFile(thumbnail);


                                request.response().end(rs.toString())
                            }
                        })

                        // Save file
                        def originalFileName = upload.filename()

                        Console.println("Original: " + originalFileName)
                        Console.println("====================")

                        try {
                            def fullPath = Settings.createUploadPath(originalFileName)

                            Console.println("New: " + fullPath)
                            Console.println("====================")

                            upload.streamToFileSystem(fullPath)
                            currentFilePath = fullPath

                        } catch (Exception ex) {
                            Console.println("==========================")
                            Console.println("Error: " + ex.getMessage())

//                            _logger.error("[upload failed]")
//                            _logger.error(ex.getMessage())
//                            _logger.error(ex.getStackTrace())

                            request.response().statusCode = 500
                            request.response().end(ex.getMessage())
                        }
                    }
                })

            }
        }
    }
}
