package com.gruas.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilDateFormat {
    private static SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public static String getStringDateFormatWithDate(Date fecha){
        return format.format(fecha);
    }

    public static String getStringDateFormatWithLongTime(long time){
        Date fechaD = new Date();
        fechaD.setTime(time);
        return format.format(fechaD);
    }
}
