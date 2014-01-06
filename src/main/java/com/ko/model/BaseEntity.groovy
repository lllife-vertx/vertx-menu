package com.ko.model

import com.google.code.morphia.annotations.Id
import com.google.code.morphia.annotations.Transient
import com.google.code.morphia.query.UpdateOperations
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.bson.types.ObjectId

/**
 * Created by recovery on 12/22/13.
 */
class BaseEntity<T> {
    @Id
    ObjectId _id

    @Transient
    String identifier

    @Transient
    private T _type;

    @Transient
    protected  static Connector _connector = new Connector()

    def UpdateOperations getUpdateOps(Class cls){
        return _connector.getDatastore().createUpdateOperations(cls)
    }

    def Result $save(){
        try {
            _connector.getDatastore().save(this)
            return  new Result(success: true, id: this._id.toString())
        }catch(e){
            return new Result(success: false, message: e.getMessage())
        }
    }

    def static <T> List<T> $findAll(Class cls){

        try {
            def rs = _connector.getDatastore().createQuery(cls).asList()
            rs.each { BaseEntity d -> d.identifier = d._id.toString() }

            return  rs
        }catch(e){
            return new ArrayList<T>()
        }
    }

    def static <T> T $findByExample(T example){
        try {
            def customer = _connector.getDatastore().queryByExample(example).get()
            customer.each { BaseEntity c -> c.identifier = c._id.toString() }
            return  customer
        }catch (e){
            println(e.getMessage())
            return  null
        }
    }

    def static <T> T $findAllByExample(T example){
        def rs = _connector.getDatastore().queryByExample(example).asList()
        rs.each { BaseEntity c -> c.identifier = c._id.toString() }
        return  rs;
    }

    def String $toJson(){
        def out = JsonOutput.toJson(this)
        return  out
    }

    def static String $toJson(Object obj){
        def out = JsonOutput.toJson(obj)
        return  out
    }

    def static Object $fromJson(String json){
        def rs = new JsonSlurper().parseText(json);
        return  rs;
    }
}
