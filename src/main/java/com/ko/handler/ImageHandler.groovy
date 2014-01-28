package com.ko.handler

import com.ko.model.ImageInfo
import com.ko.utility.HeaderUtility
import com.ko.utility.Settings
import com.ko.utility.StaticLogger
import net.coobird.thumbnailator.Thumbnails
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerFileUpload
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.logging.Logger

/**
 * Created by recovery on 1/16/14.
 */
class ImageHandler implements HandlerPrototype<com.ko.handler.ImageHandler> {

    private static Logger _logger = StaticLogger.logger()

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
//                    def image = new ImageInfo(_id: objectId)

                    ImageInfo returnImage = ImageInfo.$findById(ImageInfo.class, objectId)

                    // return file
                    if (request.uri().contains("url")) {
                        def base = Settings.getUploadPath()
                        def full = new File(base, returnImage.path).getPath()

                        _logger.info("Return Full: " + full)

                        request.response().sendFile(full)
                    } else if (request.uri().contains("thumbnail")) {
                        def base = Settings.getUploadPath()
                        def full = new File(base, returnImage.path).getPath()
                        def thumbnail = full + "_thumbnail.jpg";

                        _logger.info("Return Thumbnail: " + thumbnail)

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

        Logger _logger = StaticLogger.logger()

        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

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
                        def originalFileName = upload.filename().replaceAll(" ", "")
                        _logger.info("Original: " + originalFileName)

                        try {
                            def fullPath = Settings.createUploadPath(originalFileName)
                            _logger.info("New: " + fullPath)

                            upload.streamToFileSystem(fullPath)
                            currentFilePath = fullPath

                        } catch (Exception ex) {
                            _logger.error("<Upload Failed>")
                            _logger.error(ex.getMessage())
                            _logger.error(ex.getStackTrace())

                            request.response().statusCode = 500
                            request.response().end(ex.getMessage())
                        }
                    }
                })

            }
        }
    }

    @Override
    Handler<HttpServerRequest> $remove() {
        return new Handler<HttpServerRequest>() {

            @Override
            void handle(HttpServerRequest request) {

                HeaderUtility.allowOrigin(request)

                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {

//                        HeaderUtility.allowOrigin(request)

                        def id = request.params().get("id");
                        def objectId = new ObjectId(id);

                        Console.println("Remove reqeust: " + id)

                        def example = new ImageInfo(_id: objectId)
                        def rs = example.$remove(ImageInfo.getClass())
                        def jsonString = rs.toString()

                        request.response().end(jsonString)
                    }
                })
            }
        };
    }
}
