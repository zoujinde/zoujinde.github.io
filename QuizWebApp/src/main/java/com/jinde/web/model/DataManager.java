package com.jinde.web.model;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.jinde.web.util.LogUtil;

public class DataManager {

    private static final String TAG = "DataManager";
    private static final String SQL_SELECT = "select ";
    private static final String SQL_INSERT = "insert ";
    private static final String SQL_DELETE = "delete ";
    private static final String SQL_UPDATE = "update ";

    // Connection Pool
    // volatile ensures the memory synchronized safely
    private static volatile DataSource sDataSource = null;

    // Private constructor
    private DataManager() {
    }

    // Get the connection pool
    private static DataSource getDataSource() {
        if (sDataSource == null) { // Must double check before synchronized
            synchronized(DataSource.class){
                if (sDataSource == null) { // Must double check after synchronized
                    String url = "jdbc:mysql://localhost:3306/learnjdbc?useSSL=false&characterEncoding=utf8";
                    /* Use Tomcat DBCP instead of HikariCP
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(url);
                    config.setUsername("root");
                    config.setPassword("rootpass");
                    config.addDataSourceProperty("connectionTimeout", "1000");
                    config.addDataSourceProperty("idleTimeout", "60000");
                    config.addDataSourceProperty("maximumPoolSize", "10");
                    System.out.println("getDataSource : " + url);
                    sDataSource = new HikariDataSource(config);
                    */
                    PoolProperties p = new PoolProperties();
                    p.setUrl(url);
                    //p.setDriverClassName("com.mysql.jdbc.Driver");
                    p.setUsername("root");
                    p.setPassword("rootpass");
                    p.setJmxEnabled(true);
                    p.setTestWhileIdle(false);
                    p.setTestOnBorrow(true);
                    p.setValidationQuery("SELECT 1");
                    p.setTestOnReturn(false);
                    p.setValidationInterval(30000);
                    p.setTimeBetweenEvictionRunsMillis(30000);
                    p.setMaxActive(10);
                    p.setInitialSize(1);
                    p.setMaxWait(10000);
                    p.setRemoveAbandonedTimeout(60);
                    p.setMinEvictableIdleTimeMillis(30000);
                    p.setMinIdle(10);
                    p.setLogAbandoned(true);
                    p.setRemoveAbandoned(true);
                    p.setJdbcInterceptors(
                      "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
                      "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
                    sDataSource = new DataSource();
                    sDataSource.setPoolProperties(p);
                }
            }
        }
        return sDataSource;
    }

    // Run SQL select
    // Return JSON string
    public static String select(String sql, Object[] values) {
        ArrayList<String> list = select(sql, values, String.class);
        StringBuilder builder = null;
        int size = list.size();
        if (size > 0) {
            builder = new StringBuilder("[\n");
            for (int i = 0; i < size; i++) {
                builder.append(list.get(i));
                if (i < size - 1) {
                    builder.append(",\n");
                } else {
                    builder.append("\n");
                }
            }
            builder.append("]\n");
        }
        return builder == null ? null : builder.toString() ;
    }

    // Run SQL select -> Return T[] array
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> select(String sql, Object[] values, Class<T> T) {
        ArrayList<T> list = new ArrayList<T>();
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            checkSqlValues(sql, values);
            cn = getDataSource().getConnection();
            ps = cn.prepareStatement(sql);
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]); // setObject(i + 1, xxx)
            }
            rs = ps.executeQuery();
            StringBuilder b = (T == String.class ? new StringBuilder() : null);
            while (rs.next()) {
                if (T == String.class) {
                    list.add((T)buildJson(rs, b));
                } else {
                    list.add(buildObject(rs, T));
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LogUtil.println(TAG, "select : " + e);
        } finally {
            close(rs);
            close(ps);
            close(cn);
        }
        return list;
    }

    // Run SQL insert
    public static String insert(String sql, Object[] values) {
        try {
            checkSqlValues(sql, values);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    // Run SQL delete
    public static String delete(String sql, Object[] values) {
        try {
            checkSqlValues(sql, values);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    // Run SQL update
    public static String update(String sql, Object[] values) {
        try {
            checkSqlValues(sql, values);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    // Build a new object
    private static <T> T buildObject(ResultSet rs, Class<T> T)
            throws InstantiationException, IllegalAccessException,
            SQLException, NoSuchFieldException, SecurityException {
        ResultSetMetaData data = rs.getMetaData();
        int count = data.getColumnCount();
        String name = null;
        T t = T.newInstance();
        for (int c = 1; c <= count; c++) {
            name = data.getColumnName(c);
            Field f = T.getField(name);
            f.set(t, rs.getObject(c));
        }
        return t;
    }

    // Build the JSON String
    private static String buildJson(ResultSet rs, StringBuilder builder) throws SQLException {
        builder.setLength(0);
        ResultSetMetaData data = rs.getMetaData();
        int count = data.getColumnCount();
        int type = 0;
        String name = null;
        builder.append("{");
        for (int c = 1; c <= count; c++) {
            type = data.getColumnType(c);
            name = data.getColumnName(c);
            builder.append("\"").append(name).append("\":");
            if (type == java.sql.Types.BIGINT || type == java.sql.Types.INTEGER) {
                builder.append(rs.getLong(c));
            } else if (type == java.sql.Types.BOOLEAN || type == java.sql.Types.BIT) {
                builder.append(rs.getBoolean(c));
            } else if (type == java.sql.Types.VARCHAR) {
                builder.append("\"").append(rs.getString(c)).append("\"");
            } else {
                builder.append(type);
            }
            if (c < count) {
                builder.append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    // Close method
    private static void close(AutoCloseable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (Exception e) {
                LogUtil.println(TAG, "close : " + e);
            }
        }
    }

    // Check SQL and values
    private static void checkSqlValues(String sql, Object[] values) throws IOException {
        //JVM default ignore assert. Use java -enableassertions （or -ea） to enable assert
        //assert sql.contains("?") && values.length > 0;
        //assert sql.startsWith(SQL_SELECT) : "Invalid SQL select";
        if (sql.contains("?") && values.length > 0) {
            // SQL must use ? and values to avoid injection
        } else {
            throw new IOException("SQL must use ? and values");
        }
        if (sql.startsWith(SQL_SELECT) || sql.startsWith(SQL_INSERT) ||
            sql.startsWith(SQL_DELETE) || sql.startsWith(SQL_UPDATE)) {
            // SQL use lower case
        } else {
            throw new IOException("Only accept lower case SQL");
        }
    }

}
