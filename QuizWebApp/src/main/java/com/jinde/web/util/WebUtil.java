package com.jinde.web.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

public class WebUtil {
    public static final String TAG = "WebUtil";
    public static final String UTF8 = "utf-8";

    public static final String ACT = "act";
    public static final String ACT_SELECT = "select";
    public static final String ACT_INSERT = "insert";
    public static final String ACT_DELETE = "delete";
    public static final String ACT_UPDATE = "update";

    public static final String DATA = "data";
    public static final String OK = "OK";
    public static final int ROWS_LIMIT = 1000;

    // SYS_TIME for table time
    private static final Timestamp SYS_TIME = new Timestamp(System.currentTimeMillis());

    // Update SYS_TIME per minute and do other task
    static {
       new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        LogUtil.println(TAG, "SYS " + SYS_TIME);
                        Thread.sleep(60000);
                        SYS_TIME.setTime(System.currentTimeMillis());
                    } catch (Exception e) {
                        LogUtil.println(TAG, "SYS " + e);
                        break;
                    }
                }
            }
        }.start();
    }

    // Private Constructor
    private WebUtil() {
    }

    // print line
    public static String getPara(HttpServletRequest req, String name) throws WebException {
        String para = req.getParameter(name);
        if (para == null) {
            throw new WebException(name + " is null");
        }
        return para;
    }

    // Build object
    public static <T> T buildObject(HttpServletRequest req, Class<T> T){
        T object = null;
        String name = null;
        try {
            object = T.newInstance();
            Field[] array = T.getFields();
            String value = null;
            Class<?> type = null;
            for (Field f : array) {
                name = f.getName();
                value = req.getParameter(name);
                if (value != null) {
                    type = f.getType();
                    //LogUtil.println(TAG, "build " + name + "=" + value + "@" + type);
                    if (type == boolean.class) {
                        f.setBoolean(object, Boolean.parseBoolean(value));
                    } else if (type == int.class) {
                        f.setInt(object, Integer.parseInt(value));
                    } else if (type == long.class) {
                        f.setLong(object, Long.parseLong(value));
                    } else {
                        f.set(object, value);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.println(TAG, name + " build " + e);
        }
        return object;
    }

    // Build object from the JSON map {...} string
    public static <T> T buildObject(String json, Class<T> T){
        //LogUtil.println(TAG, "build " + json);
        T object = null;
        String name = null;
        try {
            object = T.newInstance();
            Field[] array = T.getFields();
            Class<?> type = null;
            for (Field f : array) {
                name = f.getName();
                type = f.getType();
                if (type == boolean.class) {
                    Boolean b = JsonUtil.getBoolean(json, name);
                    f.setBoolean(object, b);
                } else if (type == int.class) {
                    Integer i = JsonUtil.getInt(json, name);
                    f.setInt(object, i);
                } else if (type == long.class) {
                    Long l = JsonUtil.getLong(json, name);
                    f.setLong(object, l);
                } else if (type == Timestamp.class) {
                    String t = JsonUtil.getString(json, name);
                    if (t != null) {
                        f.set(object, t);
                    } else {
                        f.set(object, SYS_TIME);
                    }
                } else {
                    String s = JsonUtil.getString(json, name);
                    //LogUtil.println(TAG, "build " + name + "=" + s);
                    f.set(object, (s));
                }
            }
        } catch (Exception e) {
            LogUtil.println(TAG, name + " build " + e);
            object = null;
        }
        return object;
    }

    // Get the post body
    public static String getPostBody(HttpServletRequest req) {
        String body = null;
        try {
            req.setCharacterEncoding(UTF8);
            body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            LogUtil.println(TAG, "getPostBody : " + e);
        }
        return body;
    }

}
