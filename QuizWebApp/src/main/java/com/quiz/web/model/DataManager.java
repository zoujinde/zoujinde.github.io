package com.quiz.web.model;

import java.lang.reflect.Array;
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

import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebException;
import com.quiz.web.util.WebUtil;

public class DataManager {

    public static final int PARENT_ID = -1;
    private static final String TAG = DataManager.class.getSimpleName();
    private static final int STRING_LENGTH = 200;

    // volatile ensures the memory sync safely
    private static volatile DataManager sInstance = null;
    private DataSource mDataSource = null;
    private String mUrl = "";
    private ThreadLocal<ArrayList<Object>> mLocalList = new ThreadLocal<>();

    // Single instance
    public static DataManager instance() {
        if (sInstance == null) { // Must double check before sync
            synchronized(DataSource.class){
                if (sInstance == null) { // Must double check after sync
                    sInstance = new DataManager();
                }
            }
        }
        return sInstance;
    }

    // Private constructor
    private DataManager() {
        WebUtil.startTimer();
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

    // Get thread local list
    private ArrayList<Object> getThreadLocalList() {
        ArrayList<Object> list = mLocalList.get();
        if (list == null) {
            list = new ArrayList<Object>();
            mLocalList.set(list);
        }
        return list;
    } 

    // Run SQL select
    public <T extends DataObject> T[] select(String sql, Object[] values, Class<T> type) throws Exception {
        return select(sql, values, type, null);
    }

    // Run SQL select
    public <T extends DataObject> T[] selectForUpdate(String sql, Object[] values, Class<T> type) throws Exception {
        return select(sql, values, type, WebUtil.SELECT_FOR_UPDATE);
    }

    // Run SQL select
    @SuppressWarnings("unchecked")
    private <T extends DataObject> T[] select(String sql, Object[] values, Class<T> type, String option) throws Exception {
        T[] result = null;
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            checkSqlSelect(sql, values);
            cn = mDataSource.getConnection();
            ps = cn.prepareStatement(sql);
            ps.setMaxRows(WebUtil.ROWS_LIMIT); // Must set limit
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]); // setObject(i + 1, xxx)
            }
            rs = ps.executeQuery();
            // Read lines
            ArrayList<Object> list = this.getThreadLocalList();
            list.clear();
            while (rs.next()) {
                list.add(buildObject(rs, type, option));
            }
            int count = list.size();
            if (count > 0) {
                result = (T[])Array.newInstance(type, count);
                list.toArray(result);
                list.clear(); // Must clear memory
            }
        } catch (Exception e) {
            throw new WebException("DataManager.select object : " + e);
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
        return result;
    }

    // Run SQL select -> Return JSON string
    public String select(String sql, Object[] values) throws WebException {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = null;
        try {
            checkSqlSelect(sql, values);
            cn = mDataSource.getConnection();
            ps = cn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setMaxRows(WebUtil.ROWS_LIMIT); // Must set limit
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]); // setObject(i + 1, xxx)
            }
            rs = ps.executeQuery();
            // Read lines
            rs.last();
            int count = rs.getRow();
            // Set length * count to avoid builder array copy
            StringBuilder builder = new StringBuilder(STRING_LENGTH * count);
            builder.append("[ \n");// Must add 1 space;
            rs.beforeFirst();
            while (rs.next()) {
                buildJson(rs, builder);
            }
            builder.setLength(builder.length() - 2);
            builder.append("\n]\n");
            result = builder.toString();
        } catch (Exception e) {
            throw new WebException("DataManager.select string : " + e);
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
        return result;
    }

    // Run SqlActions one by one
    public String runSql(DataObject[] actions) {
        String result = null;
        boolean changed = false;
        for (DataObject obj : actions) {
            if (obj != null && obj.getAction() != null) {
                changed = true;
                break;
            }
        }
        if (changed) {
            result = this.runSqlActions(actions);
        } else { // Avoid getConnection
            result = "Data Not Changed";
        }
        return result;
    }

    // Run SqlActions one by one
    private String runSqlActions(DataObject[] actions) {
        String result = WebUtil.OK;
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int autoId = 0;
        int tmp = 0;
        try {
            cn = mDataSource.getConnection();
            cn.setAutoCommit(false); // Begin Transaction
            StringBuilder builder = new StringBuilder(STRING_LENGTH);
            // Run the sqlAct one by one
            for (DataObject obj : actions) {
                String act = (obj == null ? null : obj.getAction());
                if (act == null) {
                    continue;
                } else if (act.equals(WebUtil.ACT_INSERT)) {
                    // LogUtil.log(TAG, "autoId = " + autoId);
                    ps = buildInsertSql(cn, obj, builder, autoId);
                    ps.executeUpdate();
                    if (obj.getAutoIdName() != null) {
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            tmp = rs.getInt(1); // Can't get again
                            obj.setAutoId(tmp);
                            // LogUtil.log(TAG, "tmp = " + tmp);
                            // Only remember the ParentObject autoId
                            if (obj.isParentObject()) {
                                autoId = tmp;
                            }
                        }
                        rs.close(); // Must close rs
                    }
                } else if (act.equals(WebUtil.ACT_UPDATE)) {
                    ps = buildUpdateSql(cn, obj, builder);
                    ps.executeUpdate();
                } else if (act.equals(WebUtil.ACT_DELETE)) {
                    ps = buildDeleteSql(cn, obj, builder);
                    ps.executeUpdate();
                } else { // Run SQl with values
                    ps = buildSql(cn, obj);
                    ps.executeUpdate();
                }
                ps.close(); // Must close ps
            }
            cn.commit(); // Commit Transaction
        } catch (Exception e) {
            //e.printStackTrace();
            result = "runSql : " + e.getMessage();
            LogUtil.log(TAG, result);
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn); // Set AutoCommit
        }
        return result;
    }

    // Build the JSON String
    private void buildJson(ResultSet rs, StringBuilder builder) throws SQLException {
        ResultSetMetaData data = rs.getMetaData();
        int count = data.getColumnCount();
        int type = 0;
        String name = null;
        String tmp = null;
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
                tmp = rs.getString(c);
                tmp = tmp == null ? "" : tmp;
                builder.append("\"").append(tmp).append("\"");
            } else if (type == java.sql.Types.TIMESTAMP) {
                builder.append("\"").append(rs.getTimestamp(c)).append("\"");
            } else {
                throw new SQLException("Unknown type : " + type);
            }
            if (c < count) {
                builder.append(", ");//Must a space
            }
        }
        builder.append("},\n");
    }

    // Build the object
    private <T extends DataObject> T buildObject(ResultSet rs, Class<T> type, String option) throws Exception {
        T result = type.newInstance();
        if (WebUtil.SELECT_FOR_UPDATE.equals(option)) {
            result.mSelectForUpdate = type.newInstance();
        }
        Field[] fields = type.getFields();
        String name = null;
        Class<?> t = null;
        Object value = null;
        for (Field f : fields) {
            name = f.getName();
            t = f.getType();
            if (t == java.sql.Timestamp.class) {
                value = rs.getTimestamp(name);
            } else {
                value = rs.getObject(name);
            }
            f.set(result, value);
            if (result.mSelectForUpdate != null) {
                f.set(result.mSelectForUpdate, value);
            }
        }
        return result;
    }

    /* Get YYYY-MM-DD
    private String getYMD(ResultSet rs, int col) throws SQLException {
        return rs.getTimestamp(col).toString().substring(0,10);
    }*/

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
    private PreparedStatement buildInsertSql(Connection cn, DataObject obj, StringBuilder builder,
            long autoId) throws SQLException, IllegalAccessException {
        String autoIdName = obj.getAutoIdName();
        builder.setLength(0);
        builder.append(WebUtil.ACT_INSERT).append(" into ");
        builder.append(obj.getTableName()).append(" (");
        Field[] array = obj.getClass().getFields();
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
        debugSQL(sql);
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
                // When we insert data to the main-item(1-N) tables
                // The main table has the autoId(AutoIncrementId) as PK
                // The item table has the (autoId, itemId) as PK
                // MySQL will use the LAST_INSERT_ID() to get autoId
                Object value = f.get(obj);
                // LogUtil.log(TAG, "autoId = " + autoId + " value=" + value + " name=" + name);
                if (autoId > 0 && value != null && value.equals(PARENT_ID)) {
                    obj.setParentObject(false);
                    ps.setObject(i++, autoId);
                } else {
                    ps.setObject(i++, value);
                }
            }
        }
        return ps;
    }

    // Build SQL update
    private PreparedStatement buildUpdateSql(Connection cn, DataObject T, StringBuilder builder)
            throws SQLException, IllegalAccessException{
        builder.setLength(0);
        builder.append(WebUtil.ACT_UPDATE).append(" ");
        builder.append(T.getTableName()).append(" set ");
        Field[] array = T.getClass().getFields();
        String[] pks = T.getPrimaryKey();
        String name = null;
        for (Field f : array) {
            name = f.getName();
            if (!contains(pks, name) && T.isChanged(f)) {
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
        debugSQL(sql);
        PreparedStatement ps = cn.prepareStatement(sql);

        // Set values
        int i = 1;
        for (Field f : array) {
            name = f.getName();
            if (!contains(pks, name) && T.isChanged(f)) {
                //LogUtil.log(TAG, name + "=" + f.get(T));
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
    private PreparedStatement buildDeleteSql(Connection cn, DataObject T, StringBuilder builder)
            throws SQLException, IllegalAccessException{
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
        debugSQL(sql);
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

    // Debug SQL
    private void debugSQL(String sql) {
        if ("Y".equals(WebUtil.getValue("debug_sql"))) {
            LogUtil.log("SQL", sql);
        }
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
    private PreparedStatement buildSql(Connection cn, DataObject sqlObj)
            throws SQLException {
        String sql = sqlObj.getAction();
        Object[] values = sqlObj.getValues();
        checkSqlDml(sql, values);
        PreparedStatement ps = cn.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
        return ps;
    }

    // Initiate quiz DB
    private void initQuizDB(String url, String user, String pass) {
        try {
            // We can put mysql jar to tomcat/lib
            Class.forName(WebUtil.JDBC_MYSQL);
            LogUtil.log(TAG, "mysql class is OK");
        } catch (ClassNotFoundException e) {
            LogUtil.log(TAG, "mysql class not found then download");
            // WebUtil.downloadMySql();
        }
        Connection cn = null;
        try {
            String script = WebUtil.getWebInfPath() + "QuizDB.sql";
            LogUtil.log(TAG, script);
            String file = WebUtil.readFile(script);
            // Can't use mDataSource, URL is different
            cn = DriverManager.getConnection(url, user, pass);
            runSqlScript(file, cn);
        } catch (Exception e) {
            LogUtil.log(TAG, e.toString());
        } finally {
            WebUtil.close(cn);
        }
    }

    // Run SQL script
    public String runSqlScript(String script, Connection cn) {
        String result = WebUtil.OK;
        PreparedStatement ps = null;
        try {
            String[] array = script.split("\n");
            if (cn == null) {
                cn = mDataSource.getConnection();
            }
            StringBuilder builder = new StringBuilder(STRING_LENGTH);
            for (String line : array) {
                line = line.trim();
                if (line.length() <= 0 || line.startsWith("-- ")) {
                    continue;
                }
                builder.append(line);
                if (line.endsWith(";")) {
                    line = builder.toString();
                    builder.setLength(0); // Must clear memory
                    LogUtil.log(TAG, line);
                    ps = cn.prepareStatement(line);
                    ps.execute();
                    ps.close();
                }
            }
        } catch (Exception e) {
            result = "runSqlScript : " + e.getMessage();
            LogUtil.log(TAG, result);
        } finally {
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
        return result;
    }

    // Dump table data
    public String dump(String file) {
        String result = WebUtil.OK;
        /* SQL for dump
        String SQL_DUMP = "SELECT COLUMN_NAME " +
                " FROM INFORMATION_SCHEMA.COLUMNS " +
                " WHERE TABLE_SCHEMA='quiz' and TABLE_NAME=? " +
                " order by ordinal_position ";*/ 
        return result;
    }

}
