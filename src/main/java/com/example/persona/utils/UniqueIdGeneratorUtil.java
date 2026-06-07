package com.example.persona.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class UniqueIdGeneratorUtil {

    private UniqueIdGeneratorUtil() {}

    public static String getUniqueKey() {
        String uniqueID = UUID.randomUUID().toString().replace("-", "");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(new Date()) + uniqueID + " ";
    }
}
