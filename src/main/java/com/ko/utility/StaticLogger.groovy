package com.ko.utility

import org.vertx.java.core.logging.Logger

/**
 * Created by recovery on 1/28/14.
 */
public class StaticLogger {

    private static Logger _logger = null;
    private StaticLogger(){  }

    public static void init(Logger logger){
        _logger = logger;
    }

    public static Logger logger(){
        return _logger;
    }
}
