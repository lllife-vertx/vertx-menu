package com.ko.handler

import com.google.code.morphia.query.Query
import com.ko.model.BaseEntity
import com.ko.model.BranchInfo
import com.ko.model.CategoryInfo
import com.ko.model.Connector
import com.ko.model.DeviceInfo
import com.ko.model.PIRInfo
import com.ko.model.ProductInfo
import com.ko.model.SonicInfo
import com.ko.model.TouchInfo
import com.ko.model.client.QueryInfo
import com.ko.utility.HeaderUtility
import com.ko.utility.StaticLogger
import org.bson.types.ObjectId
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.http.HttpServerRequest

import java.text.SimpleDateFormat

/**
 * Created by recovery on 2/25/14.
 */
public class ReportHandler implements HandlerPrototype<com.ko.handler.ReportHandler> {

    @Override
    Handler<HttpServerRequest> $all() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $byId() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $byExample() {
        return null;
    }

    Handler<HttpServerRequest> $queryReport() {

        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                HeaderUtility.allowOrigin(request);

                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {
                        def body = buffer.getString(0, buffer.length());

                        QueryInfo info = QueryInfo.$fromJson(body);
                        def result = queryReport(info);

                        // Omit touch infos && pir infos
                        // Because not need in chart...
                        def value = new HashMap()
                        value.columnNames = result.columnNames
                        value.totalTouchs = []
                        value.totalPirs = []
                        value.totalSonics = []

                        result.columnNames.each { c ->
                            value.totalTouchs.add(result.touchInfos[c].size())
                            value.totalPirs.add(result.pirInfos[c].size())
                            value.totalSonics.add(result.sonicInfos[c].size())
                        }

                        def returnJson = BaseEntity.$toJson(value)
                        request.response().end(returnJson);
                    }
                });
            }
        };
    }

    Handler<HttpServerRequest> $querySummary() {
        return new Handler<HttpServerRequest>() {

            @Override
            void handle(HttpServerRequest request) {
                HeaderUtility.allowOrigin(request);

                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {

                        // Read query info
                        def body = buffer.getString(0, buffer.length());
                        QueryInfo query = QueryInfo.$fromJson(body);

                        // Start query
                        def report = queryReport(query);

                        // Filter for device
                        List<DeviceInfo> devices = DeviceInfo.$findAll(DeviceInfo)
                        List<BranchInfo> branchs = BranchInfo.$findAll(BranchInfo)

                        def qb = query.branch != ""
                        if (qb) {
                            branchs = branchs.findAll { it.identifier == query.branch }
                        }

                        devices = devices.findAll {
                            def ok = false;
                            branchs.each { b ->
                                def match = b.deviceIds.contains(it.identifier)
                                if (match) {
                                    ok = true
                                    it.createBy = b.name
                                }
                            }
                            return ok
                        }

                        devices = devices.sort { it.createBy }

                        def map = new HashMap()
                        map.columnNames = report.columnNames
                        map.touchInfos = report.touchInfos
                        map.pirInfos = report.pirInfos
                        map.sonicInfos = report.sonicInfos
                        map.devices = devices
                        devices.each {
                            it.identifier = it._id.toString()
                        }

                        def jsonString = BaseEntity.$toJson(map)
                        request.response().end(jsonString);
                    }
                })
            }
        }
    }

    @Override
    Handler<HttpServerRequest> $add() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $upload() {
        return null
    }

    @Override
    Handler<HttpServerRequest> $remove() {
        return null
    }

    private ReturnObject queryYearly(QueryInfo query, Query<TouchInfo> ds, Query<PIRInfo> pir, Query<SonicInfo> sonic) {
        def returnObject = new ReturnObject()

        // touch
        ds.field("year").greaterThanOrEq(query.yearlyFrom)
        ds.field("year").lessThanOrEq(query.yearlyTo)

        // pir
        pir.field("year").greaterThanOrEq(query.yearlyFrom)
        pir.field("year").lessThanOrEq(query.yearlyTo)

        // sonic
        sonic.field("year").greaterThanOrEq(query.yearlyFrom)
        sonic.field("year").lessThanOrEq(query.yearlyTo)

        // touch
        def result = ds.findAll().toList()
        def pirResult = pir.findAll().toList()
        def sonicResult = sonic.findAll().toList()

        if (query.groupByTime) {
            returnObject = groupByTime(query, result, pirResult, sonicResult)

        } else {
            def pirGroup = pirResult.groupBy { it.year }
            def group = result.groupBy { it.year }
            def sonicGroup = result.groupBy { it.year }
            (query.yearlyFrom..query.yearlyTo).each { year ->
                def touchs = [], pirs = [], sonics = [];
                if (group.containsKey(year)) touchs = group[year]
                if (pirGroup.containsKey(year)) pirs = pirGroup[year]
                if (sonicGroup.containsKey(year)) pirs =  sonicGroup[year]
                def key = year.toString()
                returnObject.columnNames.add(key)
                returnObject.pirInfos.put(key, pirs)
                returnObject.touchInfos.put(key, touchs)
                returnObject.sonicInfos.put(key, sonics)
            }
        }
        returnObject
    }

    private ReturnObject queryMonthly(QueryInfo query, Query<TouchInfo> ds, Query<PIRInfo> pir, Query<SonicInfo> sonic) {
        def returnObject = new ReturnObject();

        // touch
        ds.field("month").greaterThanOrEq(query.monthlyFrom)
        ds.field("month").lessThanOrEq(query.monthlyTo)

        // pir
        pir.field("month").greaterThanOrEq(query.monthlyFrom)
        pir.field("month").lessThanOrEq(query.monthlyTo)

        // sonic
        sonic.field("month").greaterThanOrEq(query.monthlyFrom)
        sonic.field("month").lessThanOrEq(query.monthlyTo)

        def result = ds.findAll().toList()
        def pirResult = pir.findAll().toList()
        def sonicResult = sonic.findAll().toList()

        if (query.groupByTime) {
            returnObject = groupByTime(query, result, pirResult, sonicResult)
        } else {
            def group = result.groupBy { it.month }
            def pirGroup = pirResult.groupBy { it.month }
            def sonicGroup = sonicResult.groupBy { it.month }
            (query.monthlyFrom..query.monthlyTo).each { month ->
                def d = new Date(2000, month, 10)
                def format = new SimpleDateFormat("MMMM")
                def text = format.format(d)

                def count = [], pirCount = [], sonicCount = [];
                if (group.containsKey(month)) count = group[month]
                if (pirGroup.containsKey(month)) pirCount = pirGroup[month]
                if (sonicGroup.containsKey(month)) sonicCount = sonicGroup[month]

                returnObject.columnNames.add(text)
                returnObject.touchInfos.put(text, count)
                returnObject.pirInfos.put(text, pirCount)
                returnObject.sonicInfos.put(text, sonicCount)
            }
        }
        returnObject
    }

    private def ReturnObject groupByTime(QueryInfo query, List<TouchInfo> result,
                                         List<PIRInfo> pirResult, List<SonicInfo> sonicResult ) {
        def returnObject = new ReturnObject()
        def group = result.groupBy { it.hour }
        def pirGroup = pirResult.groupBy { it.hour }
        def sonicGroup = sonicResult.groupBy { it.hour }
        (query.timeFrom..query.timeTo).each { time ->
            def totals = [], pirTotals = [], sonicTotals = [];
            if (group.containsKey(time)) totals = group[time]
            if (pirGroup.containsKey(time)) pirTotals = pirGroup[time]
            if (sonicGroup.containsKey(time)) sonicTotals = sonicGroup[time]
            def form = { t ->
                if (t == 25) t = 1;
                def text = String.format("%02d.00", t)
                text
            }
            def text = form(time) + " - " + form(time + 1)
            returnObject.columnNames.add(text);
            returnObject.touchInfos.put(text, totals)
            returnObject.pirInfos.put(text, pirTotals)
            returnObject.sonicInfos.put(text, sonicTotals)
        }
        returnObject
    }

    def ReturnObject queryDaliy(QueryInfo query, Query<TouchInfo> ds, Query<PIRInfo> pir, Query<SonicInfo> sonic) {
        def returnObject = new ReturnObject();
        boolean year = query.dailyYear != -1;
        boolean month = query.dailyMonth != -1;
        // append year condition
        if (year) {
            ds.field("year").equal(query.dailyYear)
            pir.field("year").equal(query.dailyYear)
            sonic.field("year").equal(query.dailyYear)
        }
        // append month condition
        if (month) {
            ds.field("month").equal(query.dailyMonth)
            pir.field("month").equal(query.dailyMonth)
            sonic.field("month").equal(query.dailyMonth)
        }
        def result = ds.findAll().toList()
        def pirResult = pir.findAll().toList()
        def sonicResult = sonic.findAll().toList()
        if (query.groupByTime) {
            returnObject = groupByTime(query, result, pirResult, sonicResult)
        } else {

            def group = result.groupBy { it.date }
            def pirGroup = pirResult.groupBy { it.date }
            def sonicGroup= sonicResult.groupBy { it.date }

            def c = Calendar.getInstance();
            def d = new Date(query.dailyYear, query.dailyMonth, 10)
            c.setTime(d)
            def numberOfDays = c.getActualMaximum(Calendar.DATE)
            (1..numberOfDays).each { day ->
                def totals = [], pirTotals = [], sonicTotals = []
                if (group.containsKey(day)) totals = group[day]
                if (pirGroup.containsKey(day)) pirTotals = pirGroup[day]
                if (sonicGroup.containsKey(day)) sonicTotals = sonicGroup[day]

                def key = day.toString()
                returnObject.columnNames.add(key);
                returnObject.touchInfos.put(key, totals)
                returnObject.pirInfos.put(key, pirTotals)
                returnObject.sonicInfos.put(key, sonicTotals)
            }
        }
        returnObject
    }

    private ReturnObject queryReport(QueryInfo query) {

        // Create return object...
        // * columnNames = graph's columnNames
        // * touchInfos = Bar A
        // * pirInfos = Bar B
        def returnObject = new ReturnObject()

        // Get static logger from vert.x
        def logger = StaticLogger.logger()
        logger.info(BaseEntity.$toJson(query))

        // Get connector instance for query DB.
        def connector = Connector.getInstance()
        def touchDs = connector.datastore.createQuery(TouchInfo)
        def pirDs = connector.datastore.createQuery(PIRInfo)
        def sonicDs = connector.datastore.createQuery(SonicInfo)

        // Get list of information.
        List<CategoryInfo> categories = CategoryInfo.$findAll(CategoryInfo);
        List<ProductInfo> products = ProductInfo.$findAll(ProductInfo);
        List<BranchInfo> branchs = BranchInfo.$findAll(BranchInfo);
        List<DeviceInfo> devices = DeviceInfo.$findAll(DeviceInfo);

        // Check filter to apply with query
        // brach = filter device in branh
        // catB = filter product in category B
        // catC = filter product in category C
        // product = filter product that match given id
        boolean branch = query.branch != ""
        boolean catB = query.categoryB != ""
        boolean catC = query.categoryC != ""
        boolean product = query.product != ""
        boolean time = (query.timeFrom != -1) || (query.timeTo != -1)

        // focus only product
        // and ignore all category
        touchDs.field("objectType").equals("Product")

        // append time condition
        if (time) {

            touchDs.field("hour").greaterThanOrEq(query.timeFrom)
            touchDs.field("hour").lessThanOrEq(query.timeTo)

            pirDs.field("hour").greaterThanOrEq(query.timeFrom)
            pirDs.field("hour").lessThanOrEq(query.timeTo)

            sonicDs.field("hour").greaterThanOrEq(query.timeFrom)
            sonicDs.field("hour").lessThanOrEq(query.timeTo)
        }

        // Add branch condition
        // Find branch...
        // Find all devices in brach
        // Check is info match devices
        if (branch) {

            logger.info("Filter <Branch>")
            logger.info(">> $query.branch")

            BranchInfo b = branchs.find { it._id.toString() == query.branch }
            def deviceIds = b.deviceIds;
            def serials = devices.find { deviceIds.contains(it._id.toString()) }.collect { it.serialNumber }
            touchDs.field("deviceId").in(serials);

            // pir & sonic
            pirDs.field("deviceId").in(devices)
            sonicDs.field("deviceId").in(devices)
        }

        // Filter category B
        if (catB) {

            def defaultId =  "000000000000000000000000";

            // Find all category X under B
            def underB = categories.findAll { it.parentId == query.categoryB };
            List<String> ids = underB.collect { it._id.toString() }
            ids.add(defaultId)

            // Find all product under X
            def productIds = products.findAll { ids.contains(it.categoryIds[0]) }.collect { it._id.toString() }
            productIds.add(defaultId)
            touchDs.field("objectId").in(productIds)

            if (catC) {

                def productsUnderC = products.findAll { it.categoryIds[0] == query.categoryC }.collect { it._id.toString() }
                productsUnderC.add(defaultId)
                touchDs.field("objectId").in(productsUnderC)

                // Add product condition...
                if (product) {

                    def productId = query.product
                    touchDs.field("objectId").equal(productId);
                }
            }
        }

        if (query.queryType == "Daily") {
            returnObject = queryDaliy(query, touchDs, pirDs, sonicDs)
        } else if (query.queryType == "Monthly") {
            returnObject = queryMonthly(query, touchDs, pirDs, sonicDs)
        } else if (query.queryType == "Yearly") {
            returnObject = queryYearly(query, touchDs, pirDs, sonicDs)
        }

        return returnObject;
    }
}

class ReturnObject {
    List<String> columnNames = new ArrayList<String>();
    Map<String, List<TouchInfo>> touchInfos = new HashMap<>()
    Map<String, List<PIRInfo>> pirInfos = new HashMap<>()
    Map<String, List<SonicInfo>> sonicInfos = new HashMap<>()
}