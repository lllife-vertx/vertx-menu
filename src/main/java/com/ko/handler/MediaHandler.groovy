package com.ko.handler

import com.ko.model.MediaInfo
import com.ko.utility.HeaderUtility
import com.ko.utility.Settings
import com.ko.utility.StaticLogger
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.http.HttpServerFileUpload
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.logging.Logger

/**
 * Created by recovery on 2/4/14.
 */
class MediaHandler implements HandlerPrototype<MediaHandler> {
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

                    MediaInfo returnImage = MediaInfo.$findById(MediaInfo.class, objectId)

                    // return file
                    if (request.uri().contains("url")) {
                        def base = Settings.getUploadPath()
                        def full = new File(base, returnImage.path).getPath()

                        request.response().sendFile(full)
                    } else if (request.uri().contains("thumbnail")) {
                        def base = Settings.getUploadPath()
                        def full = new File(base, returnImage.path).getPath()
                        def thumbnail = full + "_thumbnail.jpg";

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
        return null
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

                                MediaInfo info = MediaInfo.$fromJson(jsonString)
                                info.path = currentFilePath.replaceAll(base, "")

                                def rs = info.$save()
                                rs.data = info;

                                request.response().end(rs.toString())
                            }
                        })

                        // Save file
                        def originalFileName = upload.filename().replaceAll(" ", "")
                        _logger.info("Original: " + originalFileName)

                        try {
                            def fullPath = Settings.createUploadPath(Settings.UploadType.VIDEO, originalFileName)
                            _logger.info("New: " + fullPath)

                            upload.streamToFileSystem(fullPath)
                            currentFilePath = fullPath

                        } catch (Exception ex) {
                            _logger.error("== Upload Failed ==")
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
        return null
    }
}
