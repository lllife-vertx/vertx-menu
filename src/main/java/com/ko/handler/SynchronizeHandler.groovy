package com.ko.handler

import com.ko.model.CategoryInfo
import com.ko.model.Connector
import com.ko.model.ImageInfo
import com.ko.model.ProductInfo
import com.ko.model.SynchronizeInfo
import com.ko.utility.Settings
import com.ko.utility.StaticLogger
import groovy.json.JsonOutput
import io.netty.util.concurrent.Promise
import org.bson.types.ObjectId
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.EventBus
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.logging.Logger
import org.vertx.java.platform.Verticle

/**
 * Created by recovery on 1/31/14.
 */
class SynchronizeHandler {

    private Logger _logger = StaticLogger.logger()
    private Verticle _verticle = null;
    private def _start = "synchronize.start"
    private def _status = "synchronize.status"

    def SynchronizeHandler(Verticle verticle) {
        this._verticle = verticle
    }

    def register() {
        def b1 = _verticle.vertx.eventBus()
        def b2 = _verticle.vertx.eventBus()

        b1.registerHandler(_start, this.$start())
        b2.registerHandler(_status, this.$status())
    }

    def HashMap createImage(Connector connector, String id) {
        def iq = connector.datastore.createQuery(ImageInfo.class)
        def img = iq.field("_id").equal(new ObjectId(id)).fetch().iterator().toList().last()

        def m = new HashMap()
        m.title = img.title
        m.description = img.description
        m.path = img.path

        return m
    }

    def HashMap createCategory(Connector connector, CategoryInfo d) {
        def c = new HashMap()
        c.imageIds = []

        c.type = "category"
        c.categoryId = d.categoryId
        c.title = d.title
        c.description = d.description
        c.imageIds = []

        // Append images
        d.imageIds.each { id ->
            def img = createImage(connector, id)
            c.imageIds.add(img)
        }

        return c
    }

    def HashMap createProduct(Connector connector, ProductInfo d) {

        def p = new HashMap()
        p.type = "product"
        p.name = d.name
        p.productid = d.productId
        p.description = d.description
        p.primaryPrice = d.primaryPrice
        p.promotionPrice = d.promotionPrice
        p.memberPrice = d.memberPrice
        p.imageIds = []

        // Append images
        d.imageIds.each { id ->
            def img = createImage(connector, id)
            p.imageIds.add(img)
        }

        // Assign category ids
        p.categoryIds = d.categoryIds
        return p
    }

    // Start bus
    def Handler<Message> $start() {

        def handler = new Handler<Message>() {

            @Override
            void handle(Message message) {

                Thread.start {

                    def syncPath = Settings.getUploadPath()
                    def dir = new File(syncPath).getParentFile().getAbsolutePath()
                    def exportPath = new File(dir , "products.json").getAbsolutePath()

                    _logger.info("== Receive Start Signal ==")
                    _logger.info("Message: " + message.body())

                    def last = new SynchronizeInfo(_lastUpdate: new Date(0L))
                    List<SynchronizeInfo> syncs = SynchronizeInfo.$findAll(SynchronizeInfo.class)

                    if (syncs.size() != 0) {
                        last = syncs.last()
                    }

                    def bus = _verticle.vertx.eventBus()
                    def connector = new Connector()
                    def pq = connector.datastore.createQuery(ProductInfo.class)
                    def cq = connector.datastore.createQuery(CategoryInfo.class)

                    // Export structure
                    def exportInfos = new HashMap()
                    exportInfos.product = []
                    exportInfos.category = []

                    // Check product.
                    bus.publish(_status, "Checking product...")
                    def lastProducts = pq.field("_lastUpdate").greaterThanOrEq(last._lastUpdate).fetch().iterator().toList()

                    // Append products
                    lastProducts.each { d ->

                        bus.publish(_status, d.$toJson())

                        def p = createProduct(connector, d)
                        exportInfos.product.add(p)
                    }

                    // Check category.
                    bus.publish(_status, "Checking category...")
                    def lastCategories = cq.field("_lastUpdate").greaterThanOrEq(last._lastUpdate).fetch().iterator().toList()

                    // Append cateogories
                    lastCategories.each { d ->

                        bus.publish(_status, d.$toJson())

                        def c = createCategory(connector, d)
                        exportInfos.category.add(c)
                    }

                    def json = JsonOutput.toJson(exportInfos)
                    json = JsonOutput.prettyPrint(json)

                    //bus.publish(_status, json)

                    // Export files
                    _logger.info("Export: " + exportPath)
                    FileWriter writer = new FileWriter(exportPath)
                    writer.write(json)
                    writer.close()

                    //last.$save()
                }
            }
        }

        return handler
    }

    // Status bus
    Handler<Message> $status() {
        def handler = new Handler<Message>() {
            @Override
            void handle(Message message) {
                def body = message.body()
                _logger.info("Status: " + body)
            }
        }

        return handler
    }
}
