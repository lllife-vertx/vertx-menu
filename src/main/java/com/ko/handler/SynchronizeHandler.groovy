package com.ko.handler

import com.ko.model.CategoryInfo
import com.ko.model.Connector
import com.ko.model.ImageInfo
import com.ko.model.MediaInfo
import com.ko.model.ProductInfo
import com.ko.model.SynchronizeInfo
import com.ko.utility.Settings
import com.ko.utility.StaticLogger
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.logging.Logger
import org.vertx.java.platform.Verticle

import java.nio.file.attribute.FileTime

/**
 * Created by recovery on 1/31/14.
 */
class SynchronizeHandler {

    private Logger _logger = StaticLogger.logger()
    private Verticle _verticle = null;

    private def _start = "synchronize.start"
    private def _status = "synchronize.status"
    private def _list = "synchronize.list"

    private Connector _connector = new Connector()

    def SynchronizeHandler(Verticle verticle) {
        this._verticle = verticle
    }

    def $register() {
        def b1 = _verticle.vertx.eventBus()
        def b2 = _verticle.vertx.eventBus()
        def b3 = _verticle.vertx.eventBus()

        b1.registerHandler(_start, this.$start())
        b2.registerHandler(_status, this.$status())
        b3.registerHandler(_list, this.$list())
    }

    def HashMap $createImage(String id) {
        def iq = _connector.datastore.createQuery(ImageInfo.class)
        def img = iq.field("_id").equal(new ObjectId(id)).fetch().iterator().toList().last()

        def m = new HashMap()
        m.title = img.title
        m.description = img.description
        m.path = img.path

        return m
    }

    def HashMap $createVideo(String id){
        def iq = _connector.datastore.createQuery(MediaInfo.class)
        def video = iq.field("_id").equal(new ObjectId(id)).fetch().iterator().toList().last()

        def v = new HashMap()
        v.title = video.title
        v.description = video.description
        v.path = video.path

        return v
    }

    def HashMap $createCategory(CategoryInfo d) {
        def isMain = false

        if (d.parentId != null) {
            CategoryInfo parent = CategoryInfo.$findById(CategoryInfo, new ObjectId(d.parentId))
            if (parent != null) {
                if (parent.parentId == null) {
                    isMain = true
                }
            }
        }

        def c = new HashMap()
        c.imageIds = []

        c.id = d._id.toString()
        c.type = "category"
        c.categoryId = d.categoryId
        c.title = d.title
        c.description = d.description
        c.imageIds = []
        c.isMain = isMain

        c._delete = d.delete

        c.lastUpdate = d.lastUpdate
        c.parentId = d.parentId

        // Append images
        d.imageIds.each { id ->
            def img = $createImage(id)
            c.imageIds.add(img)
        }

        return c
    }

    def HashMap $createProduct(ProductInfo d) {

        def p = new HashMap()
        p.id = d._id.toString()
        p.type = "product"
        p.name = d.name
        p.productid = d.productId
        p.description = d.description
        p.primaryPrice = d.primaryPrice
        p.promotionPrice = d.promotionPrice
        p.memberPrice = d.memberPrice

        p.imageIds = []
        p.videoIds = []

        p._delete = d.delete
        p.lastUpdate = d.lastUpdate
        p.highlight = d.highlight
        p.promotion = d.promotion

        // Append images
        d.imageIds.each { id ->
            def img = $createImage(id)
            p.imageIds.add(img)
        }

        d.mediaIds.each { id ->
            def video = $createVideo(id)
            p.videoIds.add(video)
        }

        // Assign category ids
        p.categoryIds = d.categoryIds
        return p
    }

    def Handler<Message> $list() {

        def handler = new Handler<Message>() {
            @Override
            void handle(Message message) {
                _logger.info("== Request List ==")

                def rs = new HashMap()
                rs.categories = SynchronizeInfo.$getUnSyncCategories(false)
                rs.products = SynchronizeInfo.$getUnSyncProducts(false)

                def jsonString = JsonOutput.toJson(rs)
                jsonString = JsonOutput.prettyPrint(jsonString)

                message.reply(jsonString)
            }
        }
        return handler
    }

    // Start bus
    def Handler<Message> $start() {

        def handler = new Handler<Message>() {

            @Override
            void handle(Message message) {

                // Start work under new thread
                Thread.start {

                    def body = message.body().toString()
                    def obj = new JsonSlurper().parseText(body)
                    boolean syncAll  = obj.syncAll

                    _logger.info("== Receive Start Signal ==")
                    _logger.info("Message: " + message.body())

                    def bus = _verticle.vertx.eventBus()

                    // Export structure
                    def exportInfos = new HashMap()
                    exportInfos.product = []
                    exportInfos.category = []

                    // Append products
                    def lastProducts =  SynchronizeInfo.$getUnSyncProducts(syncAll)


                    // Publish message
                    lastProducts.each { d ->
                        bus.publish(_status, d.$toJson())

                        def p = $createProduct(d)
                        exportInfos.product.add(p)
                    }

                    // Append cateogories
                    def lastCategories =   SynchronizeInfo.$getUnSyncCategories(syncAll)

                    // Publish message
                    lastCategories.each { d ->
                        bus.publish(_status, d.$toJson())

                        def c = $createCategory(d)
                        exportInfos.category.add(c)
                    }

                    // Export result to file
                    def json = JsonOutput.toJson(exportInfos)
                    json = JsonOutput.prettyPrint(json)
                    $toFile(json)

                    // Save reference
                    def totals = lastProducts.size() + lastCategories.size()
                    SynchronizeInfo info = new SynchronizeInfo(numberOfRecords: totals)
                    info.$save()
                }
            }
        }

        return handler
    }

    def $toFile(String json) {

        // Path
        def syncPath = Settings.getUploadPath()
        def dir = new File(syncPath).getParentFile().getAbsolutePath()

        // File name
        def fileTime = FileTime.fromMillis(new Date().time).toString().replaceAll(":", "-")
        def fileName = String.format("products-%s.json", fileTime)
        def exportPath = new File(dir, fileName).getAbsolutePath()

        // Export files
        _logger.info("Export: " + exportPath)

        // Start writing...
        FileWriter writer = new FileWriter(exportPath)
        writer.write(json)
        writer.close()
    }

    // Status bus
    Handler<Message> $status() {
        def handler = new Handler<Message>() {
            @Override
            void handle(Message message) {
                def body = message.body()
//                _logger.info("Status: " + body)
            }
        }

        return handler
    }
}
