package com.ko.utility

import com.ko.model.BaseEntity
import com.ko.model.Connector
import com.ko.model.PIRInfo
import com.ko.model.TouchInfo
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.vertx.java.core.Vertx
import org.vertx.java.core.http.HttpClient

import javax.management.remote.rmi._RMIConnection_Stub
import java.text.SimpleDateFormat

class HttpUtility {

    public static enum Endpoint {
        Touchs, Pirs
    }

    private Vertx _vertx
    private Endpoint _endpoint = Endpoint.Pirs

    public HttpUtility(Endpoint endpoint, Vertx vertx) {
        this._vertx = vertx
        this._endpoint = endpoint
    }

    public HttpClient createClient() {

        // Connect touch service
        def touchPort = Settings.getTouchPort()
        def touchHost = Settings.getTouchHost()

        def client = this._vertx.createHttpClient()
        client.setPort(touchPort)
        client.setHost(touchHost)
        client.setKeepAlive(false)

        return client;
    }

    public String getQueryUrl() {
        if (_endpoint == Endpoint.Pirs) {
            "/pirs/query"
        } else {
            "/touchs/query"
        }
    }

    private String createRequestDate(Date date) {
        def format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        def dateString = format.format(date)
        return dateString
    }

    private void addToken(Object info, Date date){
        Calendar c = Calendar.getInstance()
        c.setTime(date)

        info.year = c.get(Calendar.YEAR)
        info.month = c.get(Calendar.MONTH)
        info.date = c.get(Calendar.DAY_OF_MONTH)
        info.hour = c.get(Calendar.HOUR_OF_DAY)
        info.minute = c.get(Calendar.MINUTE)
        info.second = c.get(Calendar.MINUTE)
    }

    public Object processPIRObject(String data){
        List<HashMap> pirs = new JsonSlurper().parseText(data)

        pirs.each {

            it.remove("_id")

            def pirString = JsonOutput.toJson(it)
            PIRInfo info = BaseEntity.$fromJson(pirString)

            addToken(info, info.enterDate)

            info.$save()
        }

        return  pirs
    }

    public Object processTouchObject(String data) {

        List<HashMap> touchs = new JsonSlurper().parseText(data)
        touchs.each {

            // remove _id from remote
            it.remove("_id")

            // convert object to json
            def touchString = JsonOutput.toJson(it)

            // reconstuct object from json info
            TouchInfo touch = BaseEntity.$fromJson(touchString)

            // in date token
            addToken(touch, touch.touchDate)

            touch.$save()
        }

        return touchs
    }

    public String createPIRRequestString(){
        def connector = Connector.getInstance()
        def ds = connector.getDatastore()
        def pirInfo = ds.find(PIRInfo).order("-createDate").get()

        def lastEnter = new Date(20,1,1)
        if(pirInfo != null) lastEnter = pirInfo.enterDate
        def dateString = createRequestDate(lastEnter)

        def request = new HashMap()
        request.enterDate = dateString;

        def requestString = BaseEntity.$toJson(request)
        return requestString
    }

    public String createTouchRequestString() {

        def connector = Connector.getInstance()

        def ds = connector.getDatastore()
        def touchInfo = ds.find(TouchInfo).order("-createDate").get()

        def lastTouchDate = new Date(20, 1, 1)

        // Use persist date
        if (touchInfo != null) lastTouchDate = touchInfo.touchDate
        def dateString = createRequestDate(lastTouchDate)

        def requestObject = new HashMap()
        requestObject.touchDate = dateString

        def requestString = BaseEntity.$toJson(requestObject)
        return requestString;
    }
}
