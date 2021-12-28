package com.jinde.web.model;

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
import com.jinde.web.util.WebUtil;

public class DataManager {

    private static final String TAG = "DataManager";

    // volatile ensures the memory synchronized safely
    private static volatile DataManager sInstance = null;
    private DataSource mDataSource = null;

    // Single instance
    public static DataManager instance() {
        if (sInstance == null) { // Must double check before synchronized
            synchronized(DataSource.class){
                if (sInstance == null) { // Must double check after synchronized
                    sInstance = new DataManager();
                }
            }
        }
        return sInstance;
    }

    // Private constructor
    private DataManager() {
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
        mDataSource = new DataSource();
        mDataSource.setPoolProperties(p);
    }

    // Run SQL select
    // Return JSON string
    public String select(String sql, Object[] values) {
        ArrayList<String> list = select(sql, values, String.class);
        int size = list.size();
        String jsonStr = null;
        if (size > 0) {
            StringBuilder builder = new StringBuilder("[\n");
            for (int i = 0; i < size; i++) {
                builder.append(list.get(i));
                if (i < size - 1) {
                    builder.append(",\n");
                } else {
                    builder.append("\n");
                }
            }
            builder.append("]\n");
            jsonStr = builder.toString();
        }
        return jsonStr;
    }

    // Run SQL select -> Return T[] array
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> select(String sql, Object[] values, Class<T> T) {
        ArrayList<T> list = new ArrayList<T>();
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            checkSqlValues(sql, values);
            cn = mDataSource.getConnection();
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
    public <T extends DataObject> long insert(T[] array) {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long autoId = 0;
        try {
            cn = mDataSource.getConnection();
            cn.setAutoCommit(false); // Begin Transaction
            String autoIdName = null;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                autoIdName = array[i].getAutoIdName();
                ps = buildInsertSql(cn, array[i], builder, autoId);
                ps.executeUpdate();
                if (autoIdName != null) {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        autoId = rs.getLong(1);
                    }
                    rs.close(); // Must close rs
                }
                ps.close(); // Must close ps
            }
            cn.commit(); // Commit Transaction
        } catch (Exception e) {
            //e.printStackTrace();
            LogUtil.println(TAG, "insert : " + e);
            try {
                if (cn != null) cn.rollback();
            } catch (Exception ex) {
                LogUtil.println(TAG, "insert rollback : " + ex);
            }
        } finally {
            try {
                if (cn != null) cn.setAutoCommit(true);
            } catch (Exception e) {
                LogUtil.println(TAG, "insert finally : " + e);
            }
            close(rs);
            close(ps);
            close(cn);
        }
        return autoId;
    }

    // Run SQL delete
    public String delete(String sql, Object[] values) {
        try {
            checkSqlValues(sql, values);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    // Run SQL update
    public String update(String sql, Object[] values) {
        try {
            checkSqlValues(sql, values);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    // Build a new object
    private <T> T buildObject(ResultSet rs, Class<T> T)
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
    private String buildJson(ResultSet rs, StringBuilder builder) throws SQLException {
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
    private void close(AutoCloseable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (Exception e) {
                LogUtil.println(TAG, "close : " + e);
            }
        }
    }

    // Check SQL and values
    private void checkSqlValues(String sql, Object[] values) throws SQLException {
        //JVM default ignore assert. Use java -enableassertions （or -ea） to enable assert
        //assert sql.contains("?") && values.length > 0;
        //assert sql.startsWith(SQL_SELECT) : "Invalid SQL select";
        if (sql.contains("?") && values.length > 0) {
            // SQL must use ? and values to avoid injection
        } else {
            throw new SQLException("SQL must use ? and values");
        }
        if (sql.startsWith(WebUtil.ACT_SELECT) ||
            sql.startsWith(WebUtil.ACT_INSERT) ||
            sql.startsWith(WebUtil.ACT_UPDATE) ||
            sql.startsWith(WebUtil.ACT_DELETE)) {
            // SQL lower case
        } else {
            throw new SQLException("Only accept lower case SQL");
        }
    }

    // Build SQL insert
    private <T extends DataObject> PreparedStatement buildInsertSql(Connection cn, T T,
            StringBuilder builder, long autoId) throws SQLException, IllegalAccessException {
        String autoIdName = T.getAutoIdName();
        String slaveIdName = T.getSlaveIdName();
        builder.setLength(0);
        builder.append(WebUtil.ACT_INSERT).append(" into ");
        builder.append(T.getTableName()).append(" (");
        Field[] array = T.getClass().getFields();
        String name = null;
        for (Field f : array) {
            name = f.getName();
            if (!name.equals(autoIdName)) {
                builder.append(name).append(",");
            }
        }
        builder.setLength(builder.length() - 1);
        builder.append(") values (");
        for (Field f : array) {
            name = f.getName();
            if (!name.equals(autoIdName)) {
                builder.append("?").append(",");
            }
        }
        builder.setLength(builder.length() - 1);
        builder.append(")");
        // Build statement
        String sql = builder.toString();
        //LogUtil.println(TAG, "build : " + sql);
        PreparedStatement ps = null;
        if (autoIdName != null) {
            ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
            ps = cn.prepareStatement(sql);
        }

        // Build values
        int i = 1; // Starts from 1
        for (Field f : array) {
            name = f.getName();
            if (!name.equals(autoIdName)) {
                if (autoId > 0 && name.equals(slaveIdName)) {
                    ps.setObject(i, autoId);
                } else {
                    ps.setObject(i, f.get(T));
                }
                i++;
            }
        }
        return ps;
    }

}
