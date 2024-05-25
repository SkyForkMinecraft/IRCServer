package cn.langya.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static final String simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd '-' HH:mm:ss").format(new Date(System.currentTimeMillis()));
    public static void logInfo(String str) {
        System.out.printf("[%s] %s%n",simpleDateFormat,str);
    }

}
