package com.jinde.web.util;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

public class WebUtil {
    public static final String TAG = "WebUtil";

    public static final String ACT = "act";
    public static final String ACT_SELECT = "select";
    public static final String ACT_INSERT = "insert";
    public static final String ACT_DELETE = "delete";
    public static final String ACT_UPDATE = "update";

    // Private Constructor
    private WebUtil() {
    }

    // print line
    public static String getPara(HttpServletRequest req, String name) throws WebException {
        String para = req.getParameter(name);
        if (para == null) {
            throw new WebException("Invalid " + name);
        }
        return para;
    }

    // Build object
    public static <T> T buildObject(HttpServletRequest req, Class<T> T){
        T object = null;
        try {
            object = T.newInstance();
            Field[] array = T.getDeclaredFields();
            String name = null;
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
            LogUtil.println(TAG, e.toString());
        }
        return object;
    }

}
