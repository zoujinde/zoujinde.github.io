package com.quiz.web.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    public static final String TAG = "JsonUtil";
    public static final int SIZE = 512;

    // Private Constructor
    private JsonUtil() {
    }

    // Begin : JSON methods ///////////////////////
    // Though android provides JSONObject, but it uses LinkedHashMap to occupy a lot of memory.
    // So don't use JSONObject, use below methods to get JSON value more quickly and save memory
    private static final String[] SPACE = new String[] {
        "  ",
        "    ",
        "      ",
        "        ",
        "          ",
        "            ",
        "              ",
        "                ",
        "                  "};

    // To JSON string from keys and values
    public static String toJson(String[] keys, String[] values) {
        StringBuilder sBuilder = new StringBuilder(SIZE);
        sBuilder.append("{\n");
        if (keys.length == values.length) {
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i].trim();
                String value = values[i].trim();
                if (sBuilder.length() > 2) {
                    sBuilder.append(",\n");
                }
                sBuilder.append("\"").append(key).append("\":");
                if (value.startsWith("[") || value.startsWith("{")) {
                    sBuilder.append(value);
                } else {
                    sBuilder.append("\"").append(value).append("\"");
                }
            }
        }
        sBuilder.append("\n}");
        return sBuilder.toString();
    }

    // To JSON string from object
    public static String toJson(Object object) throws ReflectiveOperationException {
        StringBuilder sBuilder = new StringBuilder(SIZE);
        sBuilder.append("{\n");
        build(sBuilder, object, 0);
        sBuilder.append("\n}");
        return sBuilder.toString();
    }

    // Set Object by JSON string
    public static void setObject(Object object, String jsonStr, boolean setEmptyValue)
            throws ReflectiveOperationException {
        build(object, jsonStr, 0, setEmptyValue);
    }

    // Set Object by JSON string
    public static boolean setObject(Object obj, String jsonStr, String[] items)
            throws ReflectiveOperationException {
        boolean changed = false;
        Class<?> objClass = obj.getClass();
        for (String name : items) {
            Object value = null;
            Field f = objClass.getField(name);
            Class<?> type = f.getType();
            // Check long to avoid integer exception
            if (type == Long.class || type == long.class) {
                value = getLong(jsonStr, name);
            } else if (type == Integer.class || type == int.class) {
                value = getInt(jsonStr, name);
            } else if (type == Float.class || type == float.class) {
                value = getFloat(jsonStr, name);
            } else if (type == Double.class || type == double.class) {
                value = getDouble(jsonStr, name);
            } else if (type == Boolean.class || type == boolean.class) {
                value = getBoolean(jsonStr, name);
            } else if (type == String.class) {
                value = getString(jsonStr, name);
            } else if (type == java.sql.Timestamp.class) {
                value = getTimestamp(jsonStr, name);
            } else {
                throw new RuntimeException("setObject : " + name + " : unknown type : " + type);
            }
            // Check value
            if (value != null && !value.equals(f.get(obj))) {
                f.set(obj, value);
                changed = true;
            }
        }
        return changed;
    }

    // Build JSON string from object
    private static void build(StringBuilder sBuilder, Object object, int level)
            throws ReflectiveOperationException {
        Field[] fields = object.getClass().getFields();
        for (Field f : fields) {
            String name = f.getName();
            if (name.startsWith("_")) {
                continue; // Skip the _xxx field
            }
            sBuilder.append(SPACE[level]).append("\"").append(name).append("\":");
            Object value = f.get(object);
            String type = f.getType().getName();
            if (type.equals("java.util.List")) {
                if (value != null) {
                    sBuilder.append("[\n");
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        type = item.getClass().getName();
                        if (type.equals("java.lang.String")) {
                            sBuilder.append(SPACE[level + 1]).append("\"").append(item).append("\",\n");
                        } else if (type.startsWith("java.lang.")) { // Long, Boolean etc.
                            sBuilder.append(SPACE[level + 1]).append(item).append(",\n");
                        } else {
                            sBuilder.append(SPACE[level + 1]).append("{\n");
                            build(sBuilder, item, level + 2);
                            sBuilder.append("\n");
                            sBuilder.append(SPACE[level + 1]).append("},\n");
                        }
                    }
                    removeEnd(sBuilder);
                    sBuilder.append("],\n");
                } else {
                    sBuilder.append("[],\n");
                }
            } else if (type.equals("java.lang.String")) {
                if (value == null) {
                    sBuilder.append("null,\n");
                } else {
                    sBuilder.append("\"").append(value).append("\",\n");
                }
            } else if (type.startsWith("java.lang.")) { // Long, Double, Boolean etc.
                sBuilder.append(value).append(",\n");

            } else if (!type.contains(".")) { // long, double, boolean etc.
                sBuilder.append(value).append(",\n");

            } else if (type.equals("java.sql.Timestamp")) {
                sBuilder.append("\"").append(value).append("\",\n");

            } else { // Other object
                if (value != null) {
                    sBuilder.append("{\n");
                    build(sBuilder, value, level + 1);
                    sBuilder.append("},\n");
                } else {
                    sBuilder.append("{},\n");
                }
            }
        }
        removeEnd(sBuilder);
    }

    // Remove the end string
    private static void removeEnd(StringBuilder builder) {
        // When string ends with ",\n", remove the ",\n"
        int p1 = builder.lastIndexOf(",\n");
        if (p1 == builder.length() - 2) {
            builder.setLength(p1);
        }
        // When string ends with "[\n", remove the "\n"
        p1 = builder.lastIndexOf("[\n");
        if (p1 == builder.length() - 2) {
            builder.setLength(p1 + 1);
        }
    }

    // Build object from JSONObject
    private static void build(Object object, String jsonStr, int level, boolean setEmptyValue)
            throws ReflectiveOperationException {
        String objName = object.getClass().getSimpleName();
        Field[] fields = object.getClass().getFields();
        Object item = objName;
        for (Field f : fields) {
            Class<?> type = f.getType();
            String name = f.getName();
            Object value = null;
            // Check long to avoid integer exception
            if (type == Long.class || type == long.class) {
                value = getLong(jsonStr, name);

            } else if (type == Integer.class || type == int.class) {
                value = getInt(jsonStr, name);

            } else if (type == Float.class || type == float.class) {
                value = getFloat(jsonStr, name);

            } else if (type == Double.class || type == double.class) {
                value = getDouble(jsonStr, name);

            } else if (type == Boolean.class || type == boolean.class) {
                value = getBoolean(jsonStr, name);

            } else if (type == String.class) {
                value = getString(jsonStr, name);
                if (value == null && setEmptyValue) {
                    value = "";
                }

            } else if (type == java.sql.Timestamp.class) {
                value = getTimestamp(jsonStr, name);

            } else if (type == List.class) {
                //JSONArray ja = (JSONArray) value;
                String[] ja = getArray(jsonStr, name);
                int len = (ja == null ? -1 : ja.length);
                Type[] args = getArgTypes(f.getGenericType());
                if (len > 0 && args != null && args.length > 0) {
                    Class<?> arg = Class.forName(args[0].getTypeName());
                    ArrayList<Object> list = new ArrayList<>(len);
                    for (int i = 0; i < len; i++) {
                        ja[i] = ja[i].trim();
                        if (ja[i].startsWith("{")) {
                            item = arg.newInstance(); // New object
                            build(item, ja[i], level + 1, setEmptyValue);
                            list.add(item);
                        } else if (arg == String.class) {
                            list.add(ja[i]);
                        } else if (arg == Long.class) {
                            list.add(Long.parseLong(ja[i]));
                        } else if (arg == Integer.class) {
                            list.add(Integer.parseInt(ja[i]));
                        } else if (arg == Float.class) {
                            list.add(Float.parseFloat(ja[i]));
                        } else if (arg == Double.class) {
                            list.add(Double.parseDouble(ja[i]));
                        } else if (arg == Boolean.class) {
                            list.add(Boolean.parseBoolean(ja[i]));
                        } else {
                            System.out.println("build list : skip " + ja[i]);
                        }
                    }
                    value = list;
                }
            } else {
                String jo = getString(jsonStr, name);
                if (jo != null && jo.startsWith("{") && jo.length() > 10) {
                    value = type.newInstance(); // New object
                    build(value, jo, level + 1, setEmptyValue);
                }
            }
            // Set object value
            // LogUtil.log(TAG, SPACE[level] + objName + "." + name + "=" + value);
            if (value != null) {
                f.set(object, value);
            }
        }
    }

    // Get Array from JSON
    public static String[] getArray(String json, String key) {
        String[] array = null;
        String s = (key != null ? getString(json, key) : json.trim());
        if (s != null && s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1).trim();
            if (s.length() > 0) {
                if (s.startsWith("[")) {
                    array = getArray(s, '[', ']');
                } else if (s.startsWith("{")) {
                    array = getArray(s, '{', '}');
                } else if (s.startsWith("\"")) {
                    array = getArray(s, '"', '"');
                } else { // long, boolean etc.
                    array = s.split(",");
                }
            }
        }
        return array;
    }

    // c1 : the start char : { or [
    // c2 : the ended char : } or ]
    private static String[] getArray(String str, char c1, char c2) {
        String[] array = null;
        StringBuilder sBuilder = new StringBuilder(SIZE);
        int len = str.length();
        int p1 = 0;
        int p2 = 0;
        String tag = "\n#\n";
        while (p1 < len) {
            if (str.charAt(p1) == c1) {
                p2 = findEnd(str, p1, c1, c2);
                if (p2 < 0) {
                    break;
                }
                if (sBuilder.length() > 0) sBuilder.append(tag);
                if (c1 == '"' && c2 == '"') { // Remove ""
                    sBuilder.append(str.substring(p1 + 1, p2));
                } else {
                    sBuilder.append(str.substring(p1, p2 + 1));
                }
                p1 = p2 + 1;
            } else {
                p1++;
            }
        }
        if (sBuilder.length() > 0) {
            array = sBuilder.toString().split(tag);
        }
        return array;
    }

    // Get the argument type
    private static Type[] getArgTypes(Type type) {
        Type[] types = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            types = pType.getActualTypeArguments();
        }
        return types;
    }

    // Get String
    public static String getString(String json, String key) {
        String value = null;
        // We can support the multiple levels of map {...} and array [...]
        int p1 = findStart(json, key);
        if (p1 > 0) {
            p1 = json.indexOf(":", p1) + 1;
            if (p1 > 1) {
                // Check the data type
                int end  = json.indexOf("}", p1);
                char c = 0;
                for (int i = p1; i < end; i++) {
                    if (json.charAt(i) > ' ') {
                        c = json.charAt(i);
                        p1 = i; // Must set p1
                        break;
                    }
                }
                // Find the end
                if (c == '"') {
                    end = json.indexOf("\"", p1 + 1) + 1;

                } else if (c == '{') {
                    end = findEnd(json, p1, '{', '}') + 1;

                } else if (c == '[') {
                    end = findEnd(json, p1, '[', ']') + 1;

                } else { // Other types : boolean, long, null etc.
                    int p2 = json.indexOf(",", p1);
                    if (p2 > 0 && p2 < end) {
                        end = p2;
                    }
                }
                if (end > 0) {
                    if (c == '"') { // Remove the ""
                        value = json.substring(p1 + 1, end - 1);
                    } else {
                        value = json.substring(p1, end).trim();
                        if (value.equals("null")) {
                            value = null;
                        }
                    }
                }
            }
        }
        return value;
    }

    // Find the key start position
    private static int findStart(String json, String key) {
        int pos = -1;
        int len = (json == null ? 0 : json.length());
        int keyLen = key.length();
        int level = 0;
        int p = 0;
        boolean inStr = false;
        for (int i = 0; i < len; i++) {
            if (json.charAt(i) == '"') {
                inStr = !inStr;
            }
            if (inStr) {
                /* Because the key maybe in 2 levels as below:
                { "Version" : {"key" : 201}, ==> We cannot get the low level key
                  "key" : 101 =================> We should get the top level key
                } */
                if (level == 1 && json.charAt(i) == '"') {
                    p = i + keyLen + 1;
                    if (p < len && json.charAt(p) == '"') {
                        if (json.substring(i + 1, p).equals(key)) {
                            pos = i; // Find the position
                            break;
                        }
                    }
                }
            } else { // Count the level
                if (json.charAt(i) == '{') {
                    level++;
                } else if (json.charAt(i) == '}') {
                    level--;
                }
            }
        }
        return pos;
    }

    // Find the end of {} or []
    // c1 : the start char : { or [
    // c2 : the ended char : } or ]
    private static int findEnd(String str, int start, char c1, char c2) {
        if (str.charAt(start) != c1) {
            throw new RuntimeException("Invalid start : " + start);
        }
        if (c1 == '"' && c2 == '"') {
            return str.indexOf('"', start + 1);
        }
        int end = -1;
        int len = str.length();
        int count = 0;
        boolean inStr = false;
        for (int i = start + 1; i < len; i++) {
            if (str.charAt(i) == '"') {
                inStr = !inStr;
            }
            // Skip the string "..."
            if (!inStr) {
                if (str.charAt(i) == c1) {
                    count++;
                } else if (str.charAt(i) == c2) {
                    if (count == 0) {
                        end = i;
                        break;
                    }
                    count--;
                }
            }
        }
        return end;
    }

    // Get Boolean from JSON
    public static Boolean getBoolean(String json, String key) {
        Boolean value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null") && s.length() > 0) {
            try {
                value = Boolean.parseBoolean(s);
            } catch (RuntimeException e) {
                System.err.println("json : " + key + " : " + e);
            }
        }
        return value;
    }

    // Get Integer from JSON
    public static Integer getInt(String json, String key) {
        Integer value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null") && s.length() > 0) {
            try {
                value = Integer.parseInt(s);
            } catch (RuntimeException e) {
                System.err.println("json : " + key + " : " + e);
            }
        }
        return value;
    }

    // Get Long from JSON
    public static Long getLong(String json, String key) {
        Long value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null") && s.length() > 0) {
            try {
                value = Long.parseLong(s);
            } catch (RuntimeException e) {
                System.err.println("json : " + key + " : " + e);
            }
        }
        return value;
    }

    // Get Float from JSON
    public static Float getFloat(String json, String key) {
        Float value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null") && s.length() > 0) {
            try {
                value = Float.parseFloat(s);
            } catch (RuntimeException e) {
                System.err.println("json : " + key + " : " + e);
            }
        }
        return value;
    }

    // Get Double from JSON
    public static Double getDouble(String json, String key) {
        Double value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null") && s.length() > 0) {
            try {
                value = Double.parseDouble(s);
            } catch (RuntimeException e) {
                System.err.println("json : " + key + " : " + e);
            }
        }
        return value;
    }

    // Get Timestamp from JSON
    public static java.sql.Timestamp getTimestamp(String json, String key) {
        java.sql.Timestamp value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null") && s.length() > 0) {
            try {
                value = java.sql.Timestamp.valueOf(s);
            } catch (RuntimeException e) {
                System.err.println("json : " + key + " : " + e);
            }
        }
        return value;
    }

    // Build JSON String
    public static void buildJson(StringBuilder builder, String key, String value) {
        int len = builder.length();
        if (len <= 0) {
            builder.append("{");
        } else { // Replace the last } with ,
            builder.setCharAt(len - 1, ',');
        }
        builder.append("\"").append(key).append("\":");
        if (value.startsWith("{") || value.startsWith("[")) {
            builder.append(value);
        } else {
            builder.append("\"").append(value).append("\"");
        }
        builder.append("}");
    }

    // End : JSON methods ///////////////////////////////////////////

}
