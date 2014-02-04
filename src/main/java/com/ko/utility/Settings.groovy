package com.ko.utility

import groovy.json.JsonSlurper
import org.vertx.java.core.logging.Logger

import java.nio.file.Path
import java.nio.file.Paths
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Created by recovery on 1/16/14.
 */
class Settings {

    private static Logger _logger = StaticLogger.logger()

    private static String dbHost;
    private static String dbName;
    private static int dbPort;
    private static String uploadPath;

    public static enum UploadType { IMAGE , VIDEO }

    static  {

       def settings = loadSetting()

        dbHost = settings.dbHost
        dbName = settings.dbName
        dbPort = settings.dbPort
        uploadPath = settings.uploadPath
    }

    def static Object loadSetting(){
        Path currentPath =  Paths.get("")
        def abs = currentPath.toAbsolutePath().toString()

        _logger.info("== Working Directory ==")
        _logger.info("Path: " + abs)


        def file = new File(abs, "Settings.json");

        def obj = new JsonSlurper().parse(new FileReader(file));
        return obj;
    }

    def static String getDbHost(){
//        return "localhost"
        return dbHost
    }

    def static String getDbName(){
//        return "EMenuSystems"
        return dbName
    }

    def static int getDbPort(){
//        return 27017
        return dbPort
    }

    def static String getUploadPath() {
//        def imagePath = "/home/recovery/sources/emenu/VertxService/upload";
//        return imagePath
        return uploadPath
    }
    def static String createUploadPath(UploadType type, String fileName) {

        def imagePath = getUploadPath()

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd")
        Date date = new Date()

        def subDirectory = dateFormat.format(date)

        // Create image or video path
        if(type == UploadType.IMAGE){
           imagePath = new File(imagePath, "images").getAbsolutePath()
        }else {
            imagePath = new File(imagePath, "videos").getAbsolutePath()
        }

        def fullPath = new File(imagePath, subDirectory);

        if(!fullPath.exists()){
            fullPath.mkdirs()

            _logger.info("Mkdirs: " + fullPath.getPath());
        }

        def uuid = UUID.randomUUID().toString()
        def newFileName = uuid + "-" + fileName;

        def absoluteName = new File(fullPath, newFileName.toLowerCase()).getPath()
        return absoluteName;
    }
}
