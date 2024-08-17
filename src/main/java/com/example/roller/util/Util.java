package com.example.roller.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static double StringToNumber(String a){
        a = a.replace("'", "").replace(" ", "");
        double number = Double.parseDouble(a);
        return number;
    }
    public static Date StringToDate(String dateStr){
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            // Преобразуем строку в дату
            Date date = simpleDateFormat.parse(dateStr);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
