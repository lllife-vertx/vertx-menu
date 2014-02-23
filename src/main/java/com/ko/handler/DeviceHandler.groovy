package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.DeviceInfo
import com.ko.model.Result
import com.ko.utility.HeaderUtility
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 2/23/14.
 */
class DeviceHandler implements HandlerPrototype {
    @Override
    Handler<HttpServerRequest> $all() {
        return new Handler<HttpServerRequest>() {

            @Override
            void handle(HttpServerRequest request) {

                HeaderUtility.allowOrigin(request);

                List<DeviceInfo> infos = DeviceInfo.$findAll(DeviceInfo);
                def jsonString = DeviceInfo.$toJson(infos);
                request.response().end(jsonString);
            }
        };
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
                HeaderUtility.allowOrigin(request);

                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {
                        def body = buffer.getString(0, buffer.length());
                        DeviceInfo info = DeviceInfo.$fromJson(body);

                        Result rs = info.$save();
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
}
