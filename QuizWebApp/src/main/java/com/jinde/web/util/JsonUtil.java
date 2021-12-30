package com.jinde.web.util;

public class JsonUtil {
    public static final String TAG = "JsonUtil";

    // Private Constructor
    private JsonUtil() {
    }

    /* The sample JSON as below:
    {"act"="insert",
     "prop"={"width"=100, "height"=200},
     "data"=[
            {"user_id"=1, "user_name"="A"},
            {"user_id"=2, "user_name"="B"},
            {"user_id"=3, "user_name"="C"}
            ]
    }
    * We need get the  "insert"  by the key "act"
    * Or get the prop map  {...} by the key "prop"
    * Or get the data list [...] by the key "data"
    */

    // Get the string by the key from the JSON string
    public static String getString(String jsonStr, String key) {
        // Find the key start position
        key = "\"" + key + "\"";
        int p1 = jsonStr.indexOf(key);
        if (p1 < 0) return null; // No data

        // Find the = position
        p1 = jsonStr.indexOf("=", p1);
        if (p1 < 0) return null;

        // Find the 1st ""
        p1 = jsonStr.indexOf("\"", p1);
        if (p1 < 0) return null;
            
        // Find the 2nd ""
        int p2 = jsonStr.indexOf("\"", p1 + 1);
        if (p2 < 0) return null;

        return jsonStr.substring(p1 + 1, p2);
    }

    // Get the array by the key from the JSON string
    public static String[] getArray(String jsonStr, String key) {
        // Find the key start position
        key = "\"" + key + "\"";
        int p1 = jsonStr.indexOf(key);
        if (p1 < 0) return null;

        // Find the = position
        p1 = jsonStr.indexOf("=", p1);
        if (p1 < 0) return null;

        // Find the [
        p1 = jsonStr.indexOf("[", p1);
        if (p1 < 0) return null;
            
        // Find the ]
        // Can't support the more levels : [[],[]]
        // Only  support the single level: [{},{}]
        int p2 = jsonStr.indexOf("]", p1 + 1);
        if (p2 < 0) return null;

        // The array like [ {...}, {...}, {...} ]
        // We must remove the last }
        // Otherwise spilt("}") will get more one
        p2 = jsonStr.lastIndexOf("}", p2);
        if (p2 < 0) return null;
        String s = jsonStr.substring(p1 + 1, p2);
        return s.split("}");
    }

    // Get the Boolean by the key from the JSON string
    public static Boolean getBoolean(String jsonStr, String key) {
        // Find the key start position
        key = "\"" + key + "\"";
        int p1 = jsonStr.indexOf(key);
        if (p1 < 0) return null; // No data

        // Find the = position
        p1 = jsonStr.indexOf("=", p1);
        if (p1 < 0) return null;

        // Find the end ,
        int p2 = jsonStr.indexOf(",", p1 + 1);
        String tmp = jsonStr.substring(p1 + 1, p2);

        // Don't catch the runtime exception, let caller know it.
        return Boolean.parseBoolean(tmp.trim());
    }

    // Get the Integer by the key from the JSON string
    public static Integer getInt(String jsonStr, String key) {
        // Find the key start position
        key = "\"" + key + "\"";
        int p1 = jsonStr.indexOf(key);
        if (p1 < 0) return null; // No data

        // Find the = position
        p1 = jsonStr.indexOf("=", p1);
        if (p1 < 0) return null;

        // Find the end ,
        int p2 = jsonStr.indexOf(",", p1 + 1);
        String tmp = jsonStr.substring(p1 + 1, p2);

        // Don't catch the runtime exception, let caller know it.
        return Integer.parseInt(tmp.trim());
    }

    // Get the Long by the key from the JSON string
    public static Long getLong(String jsonStr, String key) {
        // Find the key start position
        key = "\"" + key + "\"";
        int p1 = jsonStr.indexOf(key);
        if (p1 < 0) return null; // No data

        // Find the = position
        p1 = jsonStr.indexOf("=", p1);
        if (p1 < 0) return null;

        // Find the end ,
        int p2 = jsonStr.indexOf(",", p1 + 1);
        String tmp = jsonStr.substring(p1 + 1, p2);

        // Don't catch the runtime exception, let caller know it.
        return Long.parseLong(tmp.trim());
    }

}
