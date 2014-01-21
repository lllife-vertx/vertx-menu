package com.ko.utility

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Created by recovery on 1/16/14.
 */
class Settings {

    def static String getUploadPath() {
        def imagePath = "/home/recovery/sources/emenu/VertxService/upload";
        return imagePath
    }
    def static String createUploadPath(String fileName) {

        def imagePath = getUploadPath()

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd")
        Date date = new Date()
        def subDirectory = dateFormat.format(date)

        def fullPath = new File(imagePath, subDirectory);

        if(!fullPath.exists()){
            fullPath.mkdirs()

            Console.println("Mkdir: " + fullPath.getPath());
            Console.println("==============================");
        }

        def uuid = UUID.randomUUID().toString()
        def newFileName = uuid + "-" + fileName;

        def absoluteName = new File(fullPath, newFileName.toLowerCase()).getPath()
        return absoluteName;
    }
}
