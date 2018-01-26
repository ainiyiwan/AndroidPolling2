package io.hypertrack.android_scheduler_demo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/1/25.
 */

public class DateUtil {

    public static String getDateToString() {
        long time= System.currentTimeMillis();
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss:SSS");
        return sf.format(d);
    }
}
