package com.ko.model

import com.google.code.morphia.Key
import com.google.code.morphia.annotations.Id
import com.google.code.morphia.annotations.Transient
import com.google.code.morphia.query.UpdateOperations
import com.google.gson.Gson
import com.ko.utility.StaticLogger
import com.mongodb.util.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.bsf.util.event.adapters.java_awt_event_KeyAdapter
import org.bson.types.ObjectId
import org.vertx.java.core.logging.Logger

/**
 * Created by recovery on 12/22/13.
 */
class BaseEntity<T> {

    // default logger from vert.x
    @Transient
    private  static Logger _logger = StaticLogger.logger()

    // unique mongo id
    @Id
    ObjectId _id

    // _id shadow as string
    @Transient
    String identifier

    // archive infomation include date and status
    Date _archiveDate;
    boolean archive = false;

    // delete infomation
    Date _deleteDate;
    boolean delete = false;

    // create information
    Date _createDate = Calendar.getInstance().getTime();
    String createBy;

    // update infomation
    Date _lastUpdate = Calendar.getInstance().getTime();
    String updateBy;

    // default db connector
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

    def Result $remove(Class cls) {
        def rs = _connector.getDatastore().delete(this);

        if (rs != null) {
            return new Result(success: true, data: rs);
        } else {
            return new Result(success: false, data: rs)
        }
    }

    def static <T> List<T> $findAll(Class cls) {

        _logger.trace("Find All: " + cls.name)

        try {
            def rs = _connector.getDatastore().createQuery(cls).asList()
            rs.each { BaseEntity d -> d.identifier = d._id.toString() }

            return rs
        } catch (e) {
            return new ArrayList<T>()
        }
    }

    def static Object $findById(Class cls, ObjectId id) {

//        _logger.info("Find By Id:" + id.toString())
//        _logger.info("Class: " + cls.name)

        def entry = _connector.getDatastore().get(cls, id)
        if (entry != null) {
            entry.identifier = entry._id.toString()
        }
        return entry
    }

    def static <T> T $queryBy(Class cls, HashMap<String,Object> condition){

        def db = _connector.getDatastore()
        def con = db.createQuery(cls)

        condition.each { k, v ->
            _logger.info("== Key: " + k)
            _logger.info("== Value: " + v)
//            con.criteria(k).equals(v)
            con.field(k).equal(v)
        }

        con.get()
    }

    def static <T> T $findByExample(T example) {
        try {

            def customer = _connector.getDatastore().queryByExample(example).get()
            customer.each { BaseEntity c -> c.identifier = c._id.toString() }

            _logger.info("Find By Example: " + customer._id.toString())

            return customer
        } catch (e) {

            _logger.error(e.getMessage())
            _logger.error(e.stackTrace)

            return null
        }
    }

    def static <T> T $findAllByExample(T example) {
        def rs = _connector.getDatastore().queryByExample(example).asList()
        rs.each { BaseEntity c -> c.identifier = c._id.toString() }
        return rs;
    }

    def String $toJson() {
        return $toJson(this);
    }


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


            if (entry.key.contains("\$") || entry.key.startsWith("_")) {
                Console.println("Remove:" + entry.key)
                itor.remove();
            }
        }
    }

    def static Object $fromJsonSluper(String json) {
//        json = json.replaceAll("\\\$\\\$hashKey", "xxx")

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
