package com.ko.handler

import com.ko.model.BaseEntity
import com.ko.model.BranchInfo
import com.ko.model.CategoryInfo
import com.ko.model.Connector
import com.ko.model.ProductInfo
import com.ko.model.TouchInfo
import com.ko.model.client.QueryInfo
import com.ko.utility.HeaderUtility
import com.ko.utility.StaticLogger
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

    private Object dailyReport(QueryInfo query) {
        return null;
    }

    private Object coarseCompareReport(QueryInfo query) {

        def returnObject = new HashMap()
        returnObject.columns = []
        returnObject.touchDatas = []
        returnObject.passDatas = []

        def logger = StaticLogger.logger()
        logger.info(BaseEntity.$toJson(query))

        def connector = Connector.getInstance()
        def ds = connector.datastore.createQuery(TouchInfo)

        List<CategoryInfo> categories = CategoryInfo.$findAll(CategoryInfo);
        List<ProductInfo> products = ProductInfo.$findAll(ProductInfo);
        List<BranchInfo> branchs = BranchInfo.$findAll(BranchInfo);

        boolean branch = query.branchId != ""
        boolean catB = query.categoryBId != ""
        boolean catC = query.categoryCId != ""
        boolean product = query.productId != ""

        if (query.queryType == "Daily") {

            boolean year = query.dailyYear != -1;
            boolean month = query.dailyMonth != -1;
            boolean time = (query.timeFrom != -1) || (query.timeTo != -1)

            // append year condition
            if (year) {
                ds.field("year").equal(query.dailyYear)
            }

            // append month condition
            if (month) {
                ds.field("month").equal(query.dailyMonth)
            }

            // append time condition
            if (time) {
                ds.field("hour").greaterThanOrEq(query.timeFrom)
                ds.field("hour").lessThanOrEq(query.timeTo)
            }

            // start query
            def result = ds.findAll().toList()
            def group = result.groupBy { touch -> touch.date }

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
                returnObject.passDatas.add(0)
            }

            // debug output
            def jsonString = BaseEntity.$toJson(result);
            Console.println(jsonString)

        }else if(query.queryType == "Monthly"){

            ds.field("month").greaterThanOrEq(query.monthlyFrom)
            ds.field("month").lessThanOrEq(query.monthlyTo)

            ds.field("hour").greaterThanOrEq(query.timeFrom)
            ds.field("hour").lessThanOrEq(query.timeTo)

            def result = ds.findAll().toList()
            def group = result.groupBy { it.month }

            (query.monthlyFrom..query.monthlyTo).each { month ->
                def d = new Date(2000, month, 10)

                def format = new SimpleDateFormat("MMMM")
                def text = format.format(d)

                def count = 0;
                if(group.containsKey(month)) count = group[month].count { it }

                returnObject.columns.add(text)
                returnObject.touchDatas.add(count);
                returnObject.passDatas.add(0)
            }
        } else if(query.queryType == "Yearly"){

            ds.field("year").greaterThanOrEq(query.yearlyFrom)
            ds.field("year").lessThanOrEq(query.yearlyTo)

            ds.field("hour").greaterThanOrEq(query.timeFrom)
            ds.field("hour").lessThanOrEq(query.timeTo)

            def result = ds.findAll().toList()
            def group = result.groupBy { it.year }

            (query.yearlyFrom..query.yearlyTo).each { year ->

                def count = 0;
                if(group.containsKey(year)) count = group[year].count { it }

                returnObject.columns.add(year)
                returnObject.touchDatas.add(count)
                returnObject.passDatas.add(0)
            }
        }

        return returnObject;
    }
}
