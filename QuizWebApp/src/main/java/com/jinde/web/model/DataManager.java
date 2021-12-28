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
        String jsonStr = null;
        int size = list.size();
        if (size > 0) {
            StringBuilder builder = new StringBuilder("[\n");
            for (int i = 0; i < size; i++) {
                builder.append(list.get(i));
                builder.append(",\n");
            }
            builder.setLength(builder.length() - 2);
            builder.append("\n]\n");
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

    // Run SQL statements
    // Argument : SqlAction has the nextAction link
    // This will save memory to use link instead of list
    // Return : long as AutoId
    public long runSql(SqlAction[] actions) {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long autoId = 0;
        try {
            cn = mDataSource.getConnection();
            cn.setAutoCommit(false); // Begin Transaction
            StringBuilder builder = new StringBuilder();
            // Run the sqlAct one by one
            for (SqlAction a : actions) {
                DataObject obj = a.dataObj;
                String act = a.action;
                if (act.equals(WebUtil.ACT_INSERT)) {
                    ps = buildInsertSql(cn, obj, builder, autoId);
                    ps.executeUpdate();
                    if (obj.getAutoIdName() != null) {
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {autoId = rs.getLong(1);}
                        rs.close(); // Must close rs
                    }
                } else if (act.equals(WebUtil.ACT_UPDATE)) {
                    ps = buildUpdateSql(cn, obj, builder);
                    ps.executeUpdate();
                } else if (act.equals(WebUtil.ACT_DELETE)) {
                    ps = buildDeleteSql(cn, obj, builder);
                    ps.executeUpdate();
                } else { // Run SQl with values
                    ps = buildSql(cn, a);
                    ps.executeUpdate();
                }
                ps.close(); // Must close ps
            }
            cn.commit(); // Commit Transaction
        } catch (Exception e) {
            //e.printStackTrace();
            autoId = -1; // return error id
            LogUtil.println(TAG, "runSql : " + e);
            try {
                if (cn != null) cn.rollback();
            } catch (Exception ex) {
                LogUtil.println(TAG, "runSql rollback : " + ex);
            }
        } finally {
            try {
                if (cn != null) cn.setAutoCommit(true);
            } catch (Exception e) {
                LogUtil.println(TAG, "runSql finally : " + e);
            }
            close(rs);
            close(ps);
            close(cn);
        }
        return autoId;
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
    private PreparedStatement buildInsertSql(Connection cn, DataObject T,
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

    // Build SQL update
    private PreparedStatement buildUpdateSql(Connection cn, DataObject T,
            StringBuilder builder) throws SQLException, IllegalAccessException{
        builder.setLength(0);
        builder.append(WebUtil.ACT_UPDATE).append(" ");
        builder.append(T.getTableName()).append(" set ");
        Field[] array = T.getClass().getFields();
        String[] pks = T.getPrimaryKey();
        String name = null;
        for (Field f : array) {
            name = f.getName();
            if (!contains(pks, name)) {
                builder.append(name).append("=?,");
            }
        }
        builder.setLength(builder.length() - 1);//Remove,

        // Add the update condition using the primary key
        builder.append(" where ");
        for (String k : pks) {
            builder.append(k).append("=? and ");
        }
        builder.setLength(builder.length() - 5);//Remove and
        String sql = builder.toString();
        //LogUtil.println(TAG, "build : " + sql);
        PreparedStatement ps = cn.prepareStatement(sql);

        // Set values
        int i = 1;
        for (Field f : array) {
            name = f.getName();
            if (!contains(pks, name)) {
                ps.setObject(i, f.get(T));
                i++;
            }
        }

        // Set conditions
        for (Field f : array) {
            name = f.getName();
            if (contains(pks, name)) {
                ps.setObject(i, f.get(T));
                i++;
            }
        }
        return ps;
    }

    // Build SQL delete
    private PreparedStatement buildDeleteSql(Connection cn, DataObject T,
            StringBuilder builder) throws SQLException, IllegalAccessException{
        builder.setLength(0);
        builder.append(WebUtil.ACT_DELETE).append(" from ");
        builder.append(T.getTableName()).append(" where ");
        Field[] array = T.getClass().getFields();
        String[] pks = T.getPrimaryKey();
        String name = null;
        for (Field f : array) {
            name = f.getName();
            if (contains(pks, name)) {
                builder.append(name).append("=? and ");
            }
        }
        builder.setLength(builder.length() - 5);//Remove and
        String sql = builder.toString();
        //LogUtil.println(TAG, "build : " + sql);
        PreparedStatement ps = cn.prepareStatement(sql);

        // Set conditions
        int i = 1;
        for (Field f : array) {
            name = f.getName();
            if (contains(pks, name)) {
                ps.setObject(i, f.get(T));
                i++;
            }
        }
        return ps;
    }

    // Check if the array contains the object
    private boolean contains(Object[] array, Object obj) {
        boolean found = false;
        for (Object o : array) {
            if (o.equals(obj)) {
                found = true;
                break;
            }
        }
        return found;
    }

    // Build SQL statement
    private PreparedStatement buildSql(Connection cn, SqlAction sqlObj)
            throws SQLException {
        Object[] values = sqlObj.values;
        checkSqlValues(sqlObj.sql, values);
        PreparedStatement ps = cn.prepareStatement(sqlObj.sql);
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
        return ps;
    }

}
