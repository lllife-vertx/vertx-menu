
package com.ko.router

import com.ko.model.Connector
import com.ko.model.Customer
import com.ko.model.Result
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.http.RouteMatcher

/**
 * Created by recovery on 12/21/13.
 */
class TestRouter extends RouteMatcher {

    def private _conn = new Connector()

    TestRouter(){
        this.get("/hi/:id", new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                def id = request.params().get("id")
                def customer = Customer.$findByExample(new Customer(_id: new ObjectId(id)))

                if(customer != null){
                    customer.identifier = customer._id.toString()
                    def json = JsonOutput.toJson(customer)
                    request.response().end(json)
                }else {
                    request.response().end()
                }
            }
        })

        this.get("/hi", new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                def q = _conn.getDatastore().find(Customer.class).findAll()
                q.each { d -> d.identifier = d._id.toString() }

                def json = JsonOutput.toJson(q)
                request.response().end(json)
            }
        })

        this.post("/hi", new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {
                        def json = buffer.getString(0, buffer.length())
                        def object = new JsonSlurper().parseText(json)

                        def customer = new Customer(
                                name : object.name,
                                lastName : object.lastName,
                                email : object.email,
                                address : object.address )
                        def rs = customer.$save()
                        request.response().end(rs.toString())
                    }
                })
            }
        })


        this.put("/hi", new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {

                        // real json string from body payload
                        def json = buffer.getString(0, buffer.length())
                        def object = new JsonSlurper().parseText(json)

                        // extract identifier id
                        String id = object.identifier

                        // log
                        println("Put id -> ${id}")
                        println("Object -> ${object}")

                        // get exist document by id
                        Customer customer = Customer.$findByExample(new Customer(_id: new ObjectId(id)))

                        // update properties
                        if(customer != null){
                            customer.name = object.name
                            customer.lastName = object.lastName
                            customer.email = object.email

                            def rs = customer.$save()
                            request.response().end(rs.toString())

                        }else {
                            def error = new Result(success: false, message: "Not found")
                            request.response().end(error)
                        }
                    }
                })
            }
        })

        this.noMatch(new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {
                request.response().end("error")
            }
        })
    }
}
