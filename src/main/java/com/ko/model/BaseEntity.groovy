package com.ko.model

import com.google.code.morphia.Key
import com.google.code.morphia.annotations.Id
import com.google.code.morphia.annotations.Transient
import com.google.code.morphia.query.UpdateOperations
import com.google.gson.Gson
import com.mongodb.util.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.bsf.util.event.adapters.java_awt_event_KeyAdapter
import org.bson.types.ObjectId

/**
 * Created by recovery on 12/22/13.
 */
class BaseEntity<T> {
    @Id
    ObjectId _id

    @Transient
    String identifier

    /**
     * Arhive
     */
//    Date archiveDate;
    boolean archive = false;
//
//    // Creaet
//    Date createDate = Calendar.getInstance().getTime();
//    String createBy;
//
//    // Update
//    Date lastUpdate = Calendar.getInstance().getTime();
//    String updateBy;

    @Transient
    private T _type;

    @Transient
    protected static Connector _connector = new Connector()

    def UpdateOperations getUpdateOps(Class cls) {

        return _connector.getDatastore().createUpdateOperations(cls)
    }

    def Result $save() {
        try {
            _connector.getDatastore().save(this)
            this.identifier = this._id.toString()

            return new Result(success: true, id: this._id.toString())
        } catch (e) {
            return new Result(success: false, message: e.getMessage())
        }
    }

    def static <T> List<T> $findAll(Class cls) {

        try {
            def rs = _connector.getDatastore().createQuery(cls).asList()
            rs.each { BaseEntity d -> d.identifier = d._id.toString() }

            return rs
        } catch (e) {
            return new ArrayList<T>()
        }
    }

    def static Object $findById(Class cls, ObjectId id){
        def entry = _connector.getDatastore().get(cls, id)
        if(entry != null){
            entry.identifier = entry._id.toString()
        }
        return entry
    }

    def static <T> T $findByExample(T example) {
        try {
            Console.println("==Find By Example==")

            def customer = _connector.getDatastore().queryByExample(example).get()
            customer.each { BaseEntity c -> c.identifier = c._id.toString() }

            Console.println("Example: "+ $toJson(example))
            Console.println("Found: " + customer)

            return customer
        } catch (e) {
            Console.println("==Error==")
            println(e.getMessage())

            return null
        }
    }

    def static <T> T $findAllByExample(T example) {
        def rs = _connector.getDatastore().queryByExample(example).asList()
        rs.each { BaseEntity c -> c.identifier = c._id.toString() }
        return rs;
    }

    def String $toJson() {
        /*
        def out = JsonOutput.toJson(this)
        return out
        */
        return $toJson(this);
    }

    /*
    def static String $toJson(Object obj){
        def json = JSON.serialize(obj)
        return json;
    }*/

    def static String $toJson(Object obj) {
        def out = JsonOutput.toJson(obj)
        return out
    }

    def static Object $fromJson(String json) {
        def obj = JSON.parse(json)

        removeExtraProperty(obj)

        return obj;
    }

    def private static removeExtraProperty(HashMap rs) {
        def itor = rs.entrySet().iterator()
        while (itor.hasNext()) {
            Map.Entry<String, Object> entry = itor.next();

            Console.println("Key:" + entry.key)
            Console.println("Value:" + entry.value)
            Console.println("============================")

            if (entry.key.contains("\$") || entry.key.startsWith("_")) {
                Console.println("Remove:" + entry.key)
                itor.remove();
            }
        }
    }

    def static Object $fromJsonSluper(String json) {
//        json = json.replaceAll("\\\$\\\$hashKey", "xxx")

        Console.println(json)
        Console.println("==============================")

        HashMap rs = new JsonSlurper().parseText(json)
        removeExtraProperty(rs)

        return rs
    }

    def static Object $fromJson(String json, Class cls) {
        def gson = new Gson()
        def rs = gson.fromJson(json, cls)


        return rs
    }
}
