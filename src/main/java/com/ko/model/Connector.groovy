package com.ko.model

import com.google.code.morphia.Datastore
import com.google.code.morphia.Morphia
import com.google.code.morphia.annotations.Entity
import com.ko.utility.Settings
import com.mongodb.Mongo

/**
 * Created by recovery on 12/21/13.
 */
class Connector {

    private Datastore _dr = null;

    private Connector(){

        def host = Settings.getDbHost()
        def port = Settings.getDbPort()
        def db = Settings.getDbName()

        def mongo = new Mongo(host, port)
        _dr = new Morphia().createDatastore(mongo, db)
    }

    def Datastore getDatastore(){
        return _dr
    }

    public static Connector getInstance(){
        return new Connector()
    }
}
