package com.ko.handler

import com.google.code.morphia.query.Query
import com.ko.model.BaseEntity
import com.ko.model.BranchInfo
import com.ko.model.CategoryInfo
import com.ko.model.Connector
import com.ko.model.DeviceInfo
import com.ko.model.PIRInfo
import com.ko.model.ProductInfo
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

    public enum ReportType {
        CoarseCompare, GrainedComopare
    }

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

    Handler<HttpServerRequest> $queryReport(ReportType type) {

        return new Handler<HttpServerRequest>() {
            @Override
            void handle(HttpServerRequest request) {

                HeaderUtility.allowOrigin(request);

                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    void handle(Buffer buffer) {
                        def body = buffer.getString(0, buffer.length());

                        QueryInfo info = QueryInfo.fromJson(body);
                        def result = coarseCompareReport(info)
                        def returnJson = BaseEntity.$toJson(result)

                        request.response().end(returnJson);
                    }
                });
            }
        };
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

    private void queryYearly(QueryInfo query, Query<TouchInfo> ds, Query<PIRInfo> pir, ReturnObject returnObject) {

        // touch
        ds.field("year").greaterThanOrEq(query.yearlyFrom)
        ds.field("year").lessThanOrEq(query.yearlyTo)

        // pir
        pir.field("year").greaterThanOrEq(query.yearlyFrom)
        pir.field("year").lessThanOrEq(query.yearlyTo)

//        // touch
//        ds.field("hour").greaterThanOrEq(query.timeFrom)
//        ds.field("hour").lessThanOrEq(query.timeTo)
//
//        // pir
//        pir.field("hour").greaterThanOrEq(query.timeFrom)
//        pir.field("hour").lessThanOrEq(query.timeTo)

        // touch
        def result = ds.findAll().toList()
        def group = result.groupBy { it.year }

        def pirResult = pir.findAll().toList()
        def pirGroup = pirResult.groupBy { it.year }

        (query.yearlyFrom..query.yearlyTo).each { year ->

            def count = 0;
            if (group.containsKey(year)) count = group[year].count { it }

            returnObject.columns.add(year.toString())
            returnObject.touchDatas.add(count)

            def pirCount = 0;
            if (pirGroup.containsKey(year)) pirCount = pirGroup[year].count { it }
            returnObject.passDatas.add(pirCount)
        }
    }

    private void queryMonthly(QueryInfo query, Query<TouchInfo> ds, Query<PIRInfo> pir, ReturnObject returnObject) {

        // touch
        ds.field("month").greaterThanOrEq(query.monthlyFrom)
        ds.field("month").lessThanOrEq(query.monthlyTo)

        // pir
        pir.field("month").greaterThanOrEq(query.monthlyFrom)
        pir.field("month").lessThanOrEq(query.monthlyTo)

//        // touch
//        ds.field("hour").greaterThanOrEq(query.timeFrom)
//        ds.field("hour").lessThanOrEq(query.timeTo)
//
//        // pir
//        pir.field("hour").greaterThanOrEq(query.timeFrom)
//        pir.field("hour").lessThanOrEq(query.timeTo)

        // touch
        def result = ds.findAll().toList()
        def group = result.groupBy { it.month }

        // pir
        def pirResult = pir.findAll().toList()
        def pirGroup = pirResult.groupBy { it.month }

        (query.monthlyFrom..query.monthlyTo).each { month ->
            def d = new Date(2000, month, 10)

            def format = new SimpleDateFormat("MMMM")
            def text = format.format(d)

            def count = 0;
            if (group.containsKey(month)) count = group[month].count { it }

            returnObject.columns.add(text)
            returnObject.touchDatas.add(count);

            def pirCount = 0;
            if (pirGroup.containsKey(month)) pirCount = pirGroup[month].count { it }
            returnObject.passDatas.add(pirCount)
        }
    }

    private void queryDaliy(QueryInfo query, Query<TouchInfo> ds, Query<PIRInfo> pir, ReturnObject returnObject) {
        boolean year = query.dailyYear != -1;
        boolean month = query.dailyMonth != -1;

        // append year condition
        if (year) {
            ds.field("year").equal(query.dailyYear)
            pir.field("year").equal(query.dailyYear)
        }

        // append month condition
        if (month) {
            ds.field("month").equal(query.dailyMonth)
            pir.field("month").equal(query.dailyMonth)
        }


        // start query
        def result = ds.findAll().toList()
        def group = result.groupBy { touch -> touch.date }

        def pirResult = pir.findAll().toList()
        def pirGroup = pirResult.groupBy { it.date }

        // find number of days in month (of year)
        def c = Calendar.getInstance();
        def d = new Date(query.dailyYear, query.dailyMonth, 10)
        c.setTime(d)
        def numberOfDays = c.getActualMaximum(Calendar.DATE)

        // get total touch for each day
        (1..numberOfDays).each { day ->

            // add columns
            returnObject.columns.add(day)

            // add touch data
            def totals = 0
            if (group.containsKey(day)) totals = group[day].count { it }
            returnObject.touchDatas.add(totals)

            // add pass data
            def pirTotals = 0;
            if (pirGroup.containsKey(day)) pirTotals = pirGroup[day].count { it }
            returnObject.passDatas.add(pirTotals)
        }

        // debug output
        def jsonString = BaseEntity.$toJson(result);
        Console.println(jsonString)
    }

    private Object coarseCompareReport(QueryInfo query) {

        // Create return object...
        // * columns = graph's columns
        // * touchDatas = Bar A
        // * passDatas = Bar B
        def returnObject = new ReturnObject()

        // Get static logger from vert.x
        def logger = StaticLogger.logger()
        logger.info(BaseEntity.$toJson(query))

        // Get connector instance for query DB.
        def connector = Connector.getInstance()
        def ds = connector.datastore.createQuery(TouchInfo)
        def pir = connector.datastore.createQuery(PIRInfo)

        // Get list of information.
        List<CategoryInfo> categories = CategoryInfo.$findAll(CategoryInfo);
        List<ProductInfo> products = ProductInfo.$findAll(ProductInfo);
        List<BranchInfo> branchs = BranchInfo.$findAll(BranchInfo);

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

        // append time condition
        if (time) {
            ds.field("hour").greaterThanOrEq(query.timeFrom)
            ds.field("hour").lessThanOrEq(query.timeTo)

            pir.field("hour").greaterThanOrEq(query.timeFrom)
            pir.field("hour").lessThanOrEq(query.timeTo)
        }

        // Add branch condition
        // Find branch...
        // Find all devices in brach
        // Check is info match devices
        if (branch) {

            logger.info("Filter <Branch>")
            logger.info(">> $query.branch")

            BranchInfo b = branchs.find { it._id.toString() == query.branch }
            def devices = b.deviceIds;
            ds.field("deviceId").in(devices);

            // pir
            pir.field("deviceId").in(devices);
        }

        // Filter category B
        if (catB) {

            logger.info("Filter <CategoryB>")
            logger.info(">> $query.categoryB")

            // Find all category X under B
            def underB = categories.findAll { it.parentId == query.categoryB };
            List<String> ids = underB.collect { it._id.toString() }

            // Find all product under X
            def productIds = products.findAll { ids.contains(it.categoryIds[0]) }.collect { it._id }
            ds.field("_id").in(productIds);

            if (catC) {

                logger.info("Filter <CategoryC>")
                logger.info(">> $query.categoryC")

                // Add product condition...
                if (product) {

                    logger.info("Filter <Product>")
                    logger.info(">> $query.product")

                    ds.field("objectId").equal(product);
                }
            }
        }

        if (query.queryType == "Daily") {
            queryDaliy(query, ds, pir, returnObject)
        } else if (query.queryType == "Monthly") {
            queryMonthly(query, ds, pir, returnObject)
        } else if (query.queryType == "Yearly") {
            queryYearly(query, ds, pir, returnObject)
        }

        return returnObject;
    }
}

class ReturnObject {
    List<String> columns = new ArrayList<String>();
    List<Number> touchDatas = new ArrayList<Number>();
    List<Number> passDatas = new ArrayList<Number>();
}
