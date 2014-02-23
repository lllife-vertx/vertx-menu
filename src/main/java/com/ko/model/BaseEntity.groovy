package com.ko.model

import com.google.code.morphia.annotations.Id
import com.google.code.morphia.annotations.Transient
import com.google.code.morphia.query.UpdateOperations
import com.google.gson.Gson
import com.ko.utility.StaticLogger
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.bson.types.ObjectId
import org.vertx.java.core.logging.Logger

import java.text.SimpleDateFormat

/**
 * Created by recovery on 12/22/13.
 */
class BaseEntity<T> implements Serializable {

    // default logger from vert.x
    @Transient
    private static Logger _logger = StaticLogger.logger()

    // unique mongo id
    @Id
    ObjectId _id

    // _id shadow as string
    @Transient
    String identifier

    // archive infomation include date and status
    Date archiveDate;
    boolean archive = false;

    // delete infomation
    Date deleteDate;
    boolean delete = false;

    // create information
    Date createDate // = Calendar.getInstance().getTime();
    String createBy;

    // update infomation
    Date lastUpdate // = Calendar.getInstance().getTime();
    String updateBy;

    // default db connector
    @Transient
    protected static Connector _connector = new Connector()

    def UpdateOperations getUpdateOps(Class cls) {
        return _connector.getDatastore().createUpdateOperations(cls)
    }

    def Result $save() {
        try {
            if (this._id == null) {
                this.createDate = Calendar.getInstance().getTime()
            } else {
                // def fmt = "2014-02-03T07:56:14+0000"
                def dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                //this.createDate =
            }

            this.lastUpdate = Calendar.getInstance().getTime();

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

            _logger.info("Before Filter: " + rs.size())

            rs = rs.findAll { !it.delete }.iterator().toList()

            _logger.info("Alfter Filter: " + rs.size())

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

    def static <T> T $queryBy(Class cls, HashMap<String, Object> condition) {

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
        out = JsonOutput.prettyPrint(out)

        _logger.info("== Serialize ==")
        _logger.info("Class: " + obj.class)

//        def out = JSON.serialize(obj)

        return out
    }

    def static void pareDate(Object obj, String name) {
        try {
            _logger.info("Try Parse: " + name)
            _logger.info("Value: " + obj."$name")

            // Parse date string int java native date
            // 2014-02-03T08:46:22+0000
            def dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            obj."$name" = dateForm.parse(obj."$name")

        } catch (e) {
            _logger.error("== Parse createDate Failed==")
            _logger.error(e)
        }
    }

    def static Object $fromJson(String json) {
        //def obj = JSON.parse(json)
        //removeExtraProperty(obj)

        def obj = $fromJsonSluper(json)

        pareDate(obj, "deleteDate")
        pareDate(obj, "archiveDate")
        pareDate(obj, "createDate")
        pareDate(obj, "lastUpdate")

        try {
            obj._id = new ObjectId(obj.identifier)
        } catch(e) {}

        return obj;
    }

    def private static removeExtraProperty(HashMap rs) {
        def itor = rs.entrySet().iterator()
        while (itor.hasNext()) {
            Map.Entry<String, Object> entry = itor.next();

            if (entry.key.contains("\$") || entry.key.startsWith("_")) {

                _logger.info("Remove: " + entry.key)
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

//    def static Object $fromJson(String json, Class cls) {
//        def gson = new Gson()
//        def rs = gson.fromJson(json, cls)
//        return rs
//    }
}
