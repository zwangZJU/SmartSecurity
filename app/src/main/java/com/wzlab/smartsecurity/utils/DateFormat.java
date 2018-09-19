package com.wzlab.smartsecurity.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wzlab on 2018/9/18.
 */

public class DateFormat {
    public static String ChangeFormat(String date, String format){
        Date d = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            d = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat f= new SimpleDateFormat(format);
        System.out.println(f.format(d));
        return f.format(d);


    }
}
