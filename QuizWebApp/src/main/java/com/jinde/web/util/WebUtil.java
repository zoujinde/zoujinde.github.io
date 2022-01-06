package com.jinde.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
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
    public static final int ROWS_LIMIT = 500;
    //The old driver is com.mysql.jdbc.Driver
    public static final String JDBC_MYSQL = "com.mysql.cj.jdbc.Driver";

    private static String[] sConfig = null;

    // SYS_TIME for table time
    private static final Timestamp SYS_TIME = new Timestamp(System.currentTimeMillis());

    // Update SYS_TIME per minute and do other task
    private static Thread sThread = null;

    // Must synchronized to avoid multiple threads
    public static synchronized void startTimer() {
        if (sThread == null) {
            sThread = new Thread() {
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
            };
            sThread.start();
        }
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
        T object = null;
        try {
            object = T.newInstance();
            Field[] array = T.getFields();
            Class<?> type = null;
            String name = null;
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
                    /* Don't read time fields for now
                    String t = JsonUtil.getString(json, name);
                    if (t != null) {
                        f.set(object, t);
                    }*/
                    f.set(object, SYS_TIME); // Set SYS_TIME for now
                } else {
                    String s = JsonUtil.getString(json, name);
                    //LogUtil.println(TAG, "build " + name + "=" + s);
                    f.set(object, (s));
                }
            }
        } catch (Exception e) {
            LogUtil.println(TAG, "build " + e + ", json=" + json);
            object = null;
        }
        return object;
    }

    // Get DataObject by table name
    public static Object buildObject(Class<?> type) {
        Object obj = null;
        try {
            obj = type.newInstance();
        } catch (Exception e) {
            LogUtil.println(TAG, type + " build " + e);
        }
        return obj;
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


    // Get ID range -> Return error
    public static String getIdRange(String body, Integer[] id) {
        String error = null;
        try {
            String[] range = JsonUtil.getString(body, "id_range").split("-");
            Integer id1 = Integer.parseInt(range[0].trim());
            Integer id2 = Integer.parseInt(range[1].trim());
            //LogUtil.println(TAG, id1 + " - " + id2);
            if (id1 > id2 || id1 < 0) {
                error = "Invalid id_range";
            } else if (id2 - id1 > ROWS_LIMIT) {
                error = "Invalid id_range > " + ROWS_LIMIT;
            } else {
                id[0] = id1;
                id[1] = id2;
            }
        } catch (Exception e) {
            error = "getIdRange : " + e;
        }
        return error;
    }

    // Get WebInf path
    public static String getWebInfPath() {
        String path = WebUtil.class.getResource("/").getPath();
        if (path.endsWith("/bin/")) { // Eclipse bin
            path = path.substring(0,  path.length() - 5);
            path += "/src/main/webapp/WEB-INF/";
        } else { // webapps/xxx/WEB-INF/classes/
            path = path.substring(0, path.length() - 8);
        }
        return path;
    }

    // Get value from config.ini
    public static String getValue(String key) {
        String value = null;
        if (sConfig == null) {
            String file = getWebInfPath() + "config.ini";
            LogUtil.println(TAG, "ini=" + file);
            sConfig = readFile(file).split("\n");
        }
        String line;
        for (int i = 0; i < sConfig.length; i++) {
            line = sConfig[i].trim();
            if (line.startsWith(key)) {
                int p = line.indexOf("=");
                if (p > 0) {
                    value = line.substring(p + 1).trim();
                    break;
                }
            }
        }
        return value;
    } 

    // Copy file
    public static String copyFile(File src, File dst) {
        String error = null;
        try {
            byte[] buf = new byte[8192];
            FileInputStream fis = new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dst);
            int n = 0;
            while (true) {
                n = fis.read(buf);
                if (n <= 0) {
                    break;
                }
                fos.write(buf, 0, n);
            }
            fis.close();
            fos.close();
        } catch (IOException e) {
            error = "copyFile : " + e;
            LogUtil.println(TAG, error);
        }
        return error;
    }

    // Read file
    public static String readFile(String file) {
        String result = null;
        try {
            StringBuilder builder = new StringBuilder();
            byte[] buf = new byte[8192];
            FileInputStream fis = new FileInputStream(file);
            int n = 0;
            while (true) {
                n = fis.read(buf);
                if (n <= 0) {
                    break;
                }
                builder.append(new String(buf, 0, n));
            }
            fis.close();
            result = builder.toString();
        } catch (IOException e) {
            LogUtil.println(TAG, "readFile : " + e);
        }
        return result;
    }

    // Download mysql driver
    public static void downloadMySql() {
        String web = WebUtil.getWebInfPath();
        if (web.endsWith("/src/main/webapp/WEB-INF/")) {
            LogUtil.println(TAG, "Eclipse Web");
        } else {
            File lib = new File(web + "lib");
            lib.mkdir();
            String my = "/mysql-connector-java-8.0.27.jar";
            File jar = new File(lib.getAbsoluteFile() + my);
            if (jar.exists()) {
                LogUtil.println(TAG, "mysql jar exists");
            } else {
                LogUtil.println(TAG, "mysql jar down..");
                String url = "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.27" + my;
                download(url, jar);
            }
        }
    }

    public static void download(String httpUrl, File file) {
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            URL url = new URL(httpUrl);
            HttpURLConnection cn = (HttpURLConnection) url.openConnection();
            cn.setRequestMethod("GET");
            cn.setRequestProperty("Content-Type", "application/octet-stream");
            cn.setDoInput(true);
            cn.setDoOutput(true);
            cn.setRequestProperty("Connection", "Keep-Alive");
            cn.connect();
            //int len = cn.getContentLength();
            InputStream is = cn.getInputStream();
            byte[] buf = new byte[8192];
            int n = 0;
            while (true) {
                n = is.read(buf);
                if (n <= 0) {
                    break;
                }
                fos.write(buf, 0, n);
            }
            is.close();
            fos.close();
            cn.disconnect();
        } catch (IOException e) {
            LogUtil.println(TAG, "download : " + e);
        }
    }

    // Close method
    public static void close(AutoCloseable obj) {
        if (obj != null) {
            if (obj instanceof Connection) {
                Connection cn = (Connection) obj;
                try {
                    if (!cn.getAutoCommit()) {
                        cn.rollback(); // RollBack
                    }
                } catch (Exception e) {
                    LogUtil.println(TAG, "cn.rollback : " + e);
                }
                try {
                    cn.setAutoCommit(true); // Set AutoCommit
                } catch (Exception e) {
                    LogUtil.println(TAG, "cn.setAutoC : " + e);
                }
            }
            try {
                obj.close();
            } catch (Exception e) {
                LogUtil.println(TAG, "close : " + e);
            }
        }
    }

}
