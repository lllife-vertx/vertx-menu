package com.ko.model

import com.google.code.morphia.Datastore
import com.google.code.morphia.Morphia
import com.mongodb.Mongo

/**
 * Created by recovery on 12/21/13.
 */
class Connector {

    private Datastore _dr = null;

    Connector(){
        def mongo = new Mongo("localhost", 27017)
        _dr = new Morphia().createDatastore(mongo, "vertx")
    }


    def Datastore getDatastore(){
        return _dr
    }
}
