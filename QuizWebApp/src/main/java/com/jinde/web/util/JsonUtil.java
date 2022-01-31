package com.jinde.web.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    public static final String TAG = "JsonUtil";

    // Private Static
    private static final StringBuilder sBuilder = new StringBuilder();
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

    // Private Constructor
    private JsonUtil() {
    }

    // Begin : JSON methods ///////////////////////
    // Though android provides JSONObject, but it uses LinkedHashMap to occupy a lot of memory.
    // So don't use JSONObject, use below methods to get JSON value more quickly and save memory
    public static String getString(String json, String key) {
        String value = null;
        // We can support the multiple levels of map {...} and array [...]
        int p1 = getKeyPos(json, key);
        if (p1 > 0) {
            p1 = json.indexOf(":", p1) + 1;
            if (p1 > 1) {
                int end  = json.indexOf(",", p1);
                if (end < 0) {
                    end = json.indexOf("}", p1);
                }
                // Check the data type
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

                } else {
                    // Other types : boolean, long, null etc.
                }
                if (end > 0) {
                    if (c == '"') { // Remove the ""
                        value = json.substring(p1 + 1, end - 1);
                    } else {
                        value = json.substring(p1, end).trim();
                    }
                }
            }
        }
        return value;
    }

    // Get the key position
    private static int getKeyPos(String json, String key) {
        int pos = -1;
        int p1 = 0;
        int p2 = 0;
        while (true) {
            p1 = json.indexOf(key, p2) - 1;
            p2 = p1 + key.length() + 1;
            if (p1 < 0) {
                break;
            } else if (json.charAt(p1) == '"' && json.charAt(p2) == '"') {
                /* Because the key maybe in 2 levels as below:
                 { "Version" : {"key" : 201}, ==> We cannot get the low level key
                   "key" : 101 =================> We should get the top level key
                 } */
                if (getLevel(json, p1) == 1) { // Must be top level 1
                    pos = p1;
                    break;
                }
            }
        }
        return pos;
    }

    // Get level
    private static int getLevel(String str, int pos) {
        int level = 0;
        boolean inStr = false;
        for (int i = 0; i < pos; i++) {
            if (str.charAt(i) == '"') {
                inStr = !inStr;
            }
            // Skip the string "..."
            if (!inStr) {
                if (str.charAt(i) == '{') {
                    level++;
                } else if (str.charAt(i) == '}') {
                    level--;
                }
            }
        }
        return level;
    }

    // Find the end of {} or []
    // c1 : the start char : { or [
    // c2 : the ended char : } or ]
    private static int findEnd(String str, int start, char c1, char c2) {
        if (str.charAt(start) != c1) {
            throw new RuntimeException("Invalid start : " + start);
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
        if (s != null && !s.equals("null")) {
            try {
                value = Boolean.parseBoolean(s);
            } catch (RuntimeException e) {
                System.err.println("GMMap.get : " + e);
            }
        }
        return value;
    }

    // Get Integer from JSON
    public static Integer getInt(String json, String key) {
        Integer value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null")) {
            try {
                value = Integer.parseInt(s);
            } catch (RuntimeException e) {
                System.err.println("GMMap.get : " + e);
            }
        }
        return value;
    }

    // Get Long from JSON
    public static Long getLong(String json, String key) {
        Long value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null")) {
            try {
                value = Long.parseLong(s);
            } catch (RuntimeException e) {
                System.err.println("GMMap.get : " + e);
            }
        }
        return value;
    }

    // Get Float from JSON
    public static Float getFloat(String json, String key) {
        Float value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null")) {
            try {
                value = Float.parseFloat(s);
            } catch (RuntimeException e) {
                System.err.println("GMMap.get : " + e);
            }
        }
        return value;
    }

    // Get Double from JSON
    public static Double getDouble(String json, String key) {
        Double value = null;
        String s = getString(json, key);
        if (s != null && !s.equals("null")) {
            try {
                value = Double.parseDouble(s);
            } catch (RuntimeException e) {
                System.err.println("GMMap.get : " + e);
            }
        }
        return value;
    }

    // Get Array from JSON
    public static String[] getArray(String json, String key) {
        String[] array = null;
        String s = json;
        if (key != null) {
            s = getString(json, key);
        }
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

    // c1 : the start char : { or [ or "
    // c2 : the ended char : } or ] or "
    // Synchronized for the static builder
    private synchronized static String[] getArray(String str, char c1, char c2) {
        String[] array = null;
        sBuilder.setLength(0);
        int len = str.length();
        int p = -1;
        String tag = "\n#\n";
        for (int i = 0; i < len; i++) {
            if (p < 0 && str.charAt(i) == c1) {
                p = i;
            } else if (p >=0 && str.charAt(i) == c2) {
                if (sBuilder.length() > 0) sBuilder.append(tag);
                if (c1 == '"' && c2 == '"') { // Must remove the ""
                    sBuilder.append(str.substring(p + 1, i));
                } else {
                    sBuilder.append(str.substring(p, i + 1));
                }
                p = -1;
            }
        }
        if (sBuilder.length() > 0) {
            array = sBuilder.toString().split(tag);
        }
        return array;
    }

    // Build JSON string from object
    private synchronized static void build(Object object, int level)
            throws ReflectiveOperationException {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field f : fields) {
            String name = f.getName();
            if (name.startsWith("_")) {
                continue; // Skip the _xxx field
            }
            sBuilder.append(SPACE[level]).append("\"").append(name).append("\":");
            Object value = f.get(object);
            Class<?> type = f.getType();
            if (type == List.class) {
                if (value != null) {
                    sBuilder.append("[\n");
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        if (item instanceof String) {
                            sBuilder.append(SPACE[level + 1]).append("\"").append(item).append("\",\n");
                        } else {
                            sBuilder.append(SPACE[level + 1]).append("{\n");
                            build(item, level + 2);
                            sBuilder.append("\n");
                            sBuilder.append(SPACE[level + 1]).append("},\n");
                        }
                    }
                    removeEnd(sBuilder);
                    sBuilder.append("],\n");
                } else {
                    sBuilder.append("[],\n");
                }
            } else if (type == Boolean.class || type == Integer.class || type == Long.class) {
                sBuilder.append(value).append(",\n");

            } else if (type == String.class) {
                if (value == null) {
                    sBuilder.append("null,\n");
                } else {
                    sBuilder.append("\"").append(value).append("\",\n");
                }
            } else { // Other object
                if (value != null) {
                    sBuilder.append("{\n");
                    build(value, level + 1);
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
    }

    // To JSON string from object
    public synchronized static String toJson(Object object)
            throws ReflectiveOperationException {
        sBuilder.setLength(0);
        sBuilder.append("{\n");
        build(object, 0);
        sBuilder.append("\n}");
        return sBuilder.toString();
    }

    // To Object from JSON string
    public static void toObject(String jsonStr, Object object)
            throws ReflectiveOperationException {
        //JSONObject jsonObj = new JSONObject(jsonStr);
        //GMUtil.log("build : " + jsonObj);
        build(object, jsonStr, 0);
    }

    // Build object from JSONObject
    private static void build(Object object, String jsonStr, int level)
            throws ReflectiveOperationException {
        String objName = object.getClass().getSimpleName();
        Field[] fields = object.getClass().getDeclaredFields();
        Object item = null;
        for (Field f : fields) {
            Class<?> type = f.getType();
            String name = f.getName();
            Object value = null;
            // Check long to avoid integer exception
            if (type == Long.class) {
                value = getLong(jsonStr, name);

            } else if (type == Integer.class) {
                value = getInt(jsonStr, name);

            } else if (type == Boolean.class) {
                value = getBoolean(jsonStr, name);

            } else if (type == String.class) {
                value = getString(jsonStr, name);

            } else if (type == List.class) {
                //JSONArray ja = (JSONArray) value;
                String[] ja = getArray(jsonStr, name);
                int len = (ja == null ? -1 : ja.length);
                Type[] args = getArgTypes(f.getGenericType());
                if (len > 0 && args != null && args.length > 0) {
                    Class<?> arg = Class.forName(args[0].getTypeName());
                    ArrayList<Object> list = new ArrayList<>(len);
                    for (int i = 0; i < len; i++) {
                        if (arg == String.class) {//item = ja.optString(i);
                            item = ja[i];
                            if (item != null) {
                                list.add(item);
                            }
                        } else {//JSONObject jo = ja.optJSONObject(i);
                            String jo = ja[i];
                            if (jo != null) {
                                item = arg.newInstance(); // New object
                                build(item, jo, level + 1);
                                list.add(item);
                            }
                        }
                    }
                    value = list;
                } else {
                    value = null;
                }
            } else {
                String jo = getString(jsonStr, name);
                value = type.newInstance(); // New object
                build(value, jo, level + 1);
            }
            // Set object value
            LogUtil.log("build:" + SPACE[level] + objName + "." + name + "=" + value);
            if (value != null) {
                f.set(object, value);
            }
        }
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

    // End : JSON methods ///////////////////////////////////////////

}
