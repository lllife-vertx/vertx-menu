package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.BranchInfo
import com.ko.model.Result
import com.ko.utility.HeaderUtility
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest

/**
 * Created by recovery on 2/21/14.
 */
class BranchHandler implements HandlerPrototype<BranchHandler> {
    @Override
    Handler<HttpServerRequest> $all() {
        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                // alow access origin
                HeaderUtility.allowOrigin(request);

                List<BranchInfo> infos = BranchInfo.$findAll(BranchInfo);
                def jsonString = BaseEntity.$toJson(infos);
                request.response().end(jsonString);
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

                HeaderUtility.allowOrigin(request);

                request.bodyHandler(new Handler<Buffer>(){
                    @Override
                    void handle(Buffer buffer) {
                        def jsonString = buffer.getString(0, buffer.length())
                        BranchInfo info = BaseEntity.$fromJson(jsonString);

                        Result rs = info.$save()
                        rs.data = info

                        def json = BaseEntity.$toJson(rs)
                        request.response().end(json)
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
