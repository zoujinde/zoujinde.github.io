package com.jinde.web.model;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.jinde.web.util.LogUtil;
import com.jinde.web.util.WebException;
import com.jinde.web.util.WebUtil;

public class DataManager {

    private static final String TAG = DataManager.class.getSimpleName();

    // volatile ensures the memory synchronized safely
    private static volatile DataManager sInstance = null;
    private DataSource mDataSource = null;
    private String mUrl = "";

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
        WebUtil.startTimer();
        WebUtil.downloadMySql();
        //jdbc:mysql://localhost:3306/quiz?useSSL=false&characterEncoding=utf8
        String url  = "jdbc:mysql://localhost:3306/";
        String home = System.getProperty("catalina.home");
        if (home.startsWith("/usr/")) { // AWS
            String host = WebUtil.getValue("jdbc_host");
            url = url.replace("localhost", host);
        }
        String user = WebUtil.getValue("jdbc_user");
        String pass = WebUtil.getValue("jdbc_pass");
        initQuizDB(url, user, pass);
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
        mUrl = url + "quiz";
        p.setUrl(mUrl); // Set quiz DB
        p.setDriverClassName(WebUtil.JDBC_MYSQL);
        p.setUsername(user);
        p.setPassword(pass);
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
        p.setMinIdle(1);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        mDataSource = new DataSource();
        mDataSource.setPoolProperties(p);
    }

    // Run SQL select -> Return JSON string
    public String select(String sql, Object[] values) throws WebException {
        return select(sql, values, null, null);
    }

    // Run SQL select -> Return result
    public <T extends DataObject> ArrayList<T> select(String sql, Object[] values, Class<T> type) throws WebException {
        ArrayList<T> list = new ArrayList<T>();
        select(sql, values, type, list);
        return list;
    }

    // Run SQL select -> Return result
    private <T extends DataObject> String select(String sql, Object[] values, Class<T> type, ArrayList<T> list) throws WebException {
        String result = null;
        StringBuilder builder = null;
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            checkSqlSelect(sql, values);
            cn = mDataSource.getConnection();
            ps = cn.prepareStatement(sql);
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]); // setObject(i + 1, xxx)
            }
            rs = ps.executeQuery();
            // Start the JSON string
            if (type == null) {
                builder = new StringBuilder("[ \n");//Must add 1 space
            }
            // Read lines
            int count = 0;
            while (rs.next()) {
                if (builder != null) {
                    buildJson(rs, builder);
                } else {
                    list.add(buildObject(rs, type));
                }
                count++;
                if (count > WebUtil.ROWS_LIMIT) {
                    LogUtil.println(TAG, "break on ROWS > " +  WebUtil.ROWS_LIMIT);
                    break;
                }
            }
            // End the JSON string
            if (builder != null) {
                builder.setLength(builder.length() - 2);
                builder.append("\n]\n");
                result = builder.toString();
            }
        } catch (Exception e) {
            throw new WebException("select : " + mUrl + " " + e);
        } finally {
            close(rs);
            close(ps);
            close(cn);
        }
        return result;
    }

    // Run SqlActions one by one
    // Return : result
    public String runSql(SqlAction[] actions) {
        String result = null;
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int autoId = 0;
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
                        if (rs.next()) {autoId = rs.getInt(1);}
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
            result = WebUtil.OK;
        } catch (Exception e) {
            //e.printStackTrace();
            result = e.getMessage();
            LogUtil.println(TAG, result);
            try {
                if (cn != null) cn.rollback();
            } catch (Exception ex) {
                LogUtil.println(TAG, "runSql rollback : " + ex);
            }
        } finally {
            close(rs);
            close(ps);
            close(cn); // Set AutoCommit
        }
        return result;
    }

    // Close method
    private void close(AutoCloseable obj) {
        WebUtil.close(obj);
    }

    // Build a new object
    private <T> T buildObject(ResultSet rs, Class<T> type)
            throws InstantiationException, IllegalAccessException,
            SQLException, NoSuchFieldException, SecurityException {
        ResultSetMetaData data = rs.getMetaData();
        int count = data.getColumnCount();
        String name = null;
        T obj = type.newInstance();
        for (int c = 1; c <= count; c++) {
            name = data.getColumnName(c);
            Field f = type.getField(name);
            f.set(obj, rs.getObject(c));
        }
        return obj;
    }

    // Build the JSON String
    private String buildJson(ResultSet rs, StringBuilder builder) throws SQLException {
        ResultSetMetaData data = rs.getMetaData();
        int count = data.getColumnCount();
        int type = 0;
        String name = null;
        builder.append("{");
        for (int c = 1; c <= count; c++) {
            type = data.getColumnType(c);
            name = data.getColumnName(c);
            builder.append("\"").append(name).append("\":");
            if (type == java.sql.Types.BIGINT) {
                builder.append(rs.getLong(c));
            } else if (type == java.sql.Types.TINYINT || type == java.sql.Types.INTEGER) {
                builder.append(rs.getInt(c));
            } else if (type == java.sql.Types.BOOLEAN || type == java.sql.Types.BIT) {
                builder.append(rs.getBoolean(c));
            } else if (type == java.sql.Types.VARCHAR) {
                builder.append("\"").append(rs.getString(c)).append("\"");
            } else if (type == java.sql.Types.TIMESTAMP) {
                builder.append("\"").append(getYMD(rs,c)).append("\"");
            } else {
                throw new SQLException("Unknown type : " + type);
            }
            if (c < count) {
                builder.append(", ");//Must a space
            }
        }
        builder.append("},\n");
        return builder.toString();
    }

    // Get YYYY-MM-DD
    private String getYMD(ResultSet rs, int col) throws SQLException {
        return rs.getTimestamp(col).toString().substring(0,10);
    }

    // Check SQL select
    private void checkSqlSelect(String sql, Object[] values) throws SQLException {
        //JVM default ignore assert. Use java -enableassertions （or -ea） to enable assert
        //assert sql.contains("?") && values.length > 0;
        //assert sql.startsWith(SQL_SELECT) : "Invalid SQL select";
        if (sql.contains("?") && values.length > 0) {
            // SQL must use ? and values to avoid injection
        } else {
            throw new SQLException("SQL must use ? and values");
        }
        if (sql.startsWith(WebUtil.ACT_SELECT)) {
            // SQL lower case
        } else {
            throw new SQLException("Invalid select SQL");
        }
    }

    // Check SQL DML
    private void checkSqlDml(String sql, Object[] values) throws SQLException {
        if (sql.contains("?") && values.length > 0) {
            // SQL must use ? and values to avoid injection
        } else {
            throw new SQLException("SQL must use ? and values");
        }
        if (sql.startsWith(WebUtil.ACT_INSERT) ||
            sql.startsWith(WebUtil.ACT_UPDATE) ||
            sql.startsWith(WebUtil.ACT_DELETE)) {
            // SQL lower case
        } else {
            throw new SQLException("Invalid DML SQL");
        }
    }

    // Build SQL insert
    private PreparedStatement buildInsertSql(Connection cn, DataObject T,
            StringBuilder builder, long autoId) throws SQLException, IllegalAccessException {
        String autoIdName = T.getAutoIdName();
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
        String[] pk = T.getPrimaryKey();
        int i = 1; // Starts from 1
        for (Field f : array) {
            name = f.getName();
            if (!name.equals(autoIdName)) {
                // When we insert data to the main-item(1-N) tables
                // The main table has the autoId(AutoIncrementId) as PK
                // The item table has the (autoId, itemId) as PK
                // Because we don't know the autoId, so have to set id=0
                // Then MySQL will use the LAST_INSERT_ID() to get  autoId
                // Then below codes will set the item table PK[0] = autoId
                if (autoId > 0 && f.get(T).equals(0) && contains(pk, name)) {
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
        checkSqlDml(sqlObj.sql, values);
        PreparedStatement ps = cn.prepareStatement(sqlObj.sql);
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
        return ps;
    }

    // Initiate quiz DB
    private void initQuizDB(String url, String user, String pass) {
        Connection cn = null;
        PreparedStatement ps = null;
        try {
            String script = WebUtil.getWebInfPath() + "QuizDB.sql";
            LogUtil.println(TAG, script);
            String file = WebUtil.readFile(script);
            String[] array = file.split("\n");
            // Can't use mDataSource, URL is different
            Class.forName(WebUtil.JDBC_MYSQL);
            cn = DriverManager.getConnection(url, user, pass);
            StringBuilder builder = new StringBuilder();
            for (String line : array) {
                line = line.trim();
                if (line.length() <= 0 || line.startsWith("-- ")) {
                    continue;
                }
                builder.append(line);
                if (line.endsWith(";")) {
                    line = builder.toString();
                    builder.setLength(0);
                    LogUtil.println(TAG, line);
                    ps = cn.prepareStatement(line);
                    ps.execute();
                    ps.close();
                }
            }
        } catch (Exception e) {
            LogUtil.println(TAG, e.toString());
        } finally {
            close(ps);
            close(cn);
        }
    }

}
