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

    public static final Byte PARENT_ID = Byte.MIN_VALUE;
    private static final String TAG = DataManager.class.getSimpleName();
    private static final int STRING_LENGTH = 200;

    // volatile ensures the memory synchronized safely
    private static volatile DataManager sInstance = null;
    private DataSource mDataSource = null;
    private String mUrl = "";
    private ThreadLocal<ArrayList<Object>> mLocalList = new ThreadLocal<>();

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

    // Run SQL select -> Return JSON string
    @SuppressWarnings("unchecked")
    public <T extends DataObject> T[] select(String sql, Object[] values, Class<T> type) throws Exception {
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
                list.add(buildObject(rs, type));
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
    // Return : result
    public String runSql(DataObject[] actions) {
        String result = null;
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
                if (obj == null) continue; 
                String act = obj.getAction();
                if (act.equals(WebUtil.ACT_INSERT)) {
                    ps = buildInsertSql(cn, obj, builder, autoId);
                    ps.executeUpdate();
                    if (obj.getAutoIdName() != null) {
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            tmp = rs.getInt(1); // Can't get again
                            obj.setAutoId(tmp);
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
            result = WebUtil.OK;
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
    private <T extends DataObject> T buildObject(ResultSet rs, Class<T> type) throws Exception {
        T result = type.newInstance();
        Field[] fields = type.getFields();
        String name = null;
        Class<?> t = null;
        for (Field f : fields) {
            name = f.getName();
            t = f.getType();
            if (t == java.sql.Timestamp.class) {
                f.set(result, rs.getTimestamp(name));
            } else {
                f.set(result, rs.getObject(name));
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
                // When we insert data to the main-item(1-N) tables
                // The main table has the autoId(AutoIncrementId) as PK
                // The item table has the (autoId, itemId) as PK
                // MySQL will use the LAST_INSERT_ID() to get autoId
                Object value = f.get(obj);
                if (autoId > 0 && PARENT_ID.equals(value)) {
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
        //LogUtil.log(TAG, "build : " + sql);
        PreparedStatement ps = cn.prepareStatement(sql);

        // Set values
        int i = 1;
        for (Field f : array) {
            name = f.getName();
            if (!contains(pks, name)) {
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

    // Get data actions according to the old and new data
    public DataObject[] getActions(DataObject[] oldData, DataObject[] newData) throws ReflectiveOperationException {
        int length = newData.length;
        if (oldData != null) {
            length += oldData.length;
        } 
        DataObject[] actions = new DataObject[length];
        int index = 0;
        // Set the insert or update actions
        for (DataObject newObj : newData) {
            DataObject found = null;
            if (oldData != null) {
                for (DataObject oldObj : oldData) {
                    if (equalPK(oldObj, newObj)) { // Compare PK
                        oldObj.setAction(WebUtil.ACT); // Add mark
                        found = oldObj;
                        break;
                    }
                }
            }
            if (found == null) {
                newObj.setAction(WebUtil.ACT_INSERT);
                actions[index++] = newObj;
            } else if (!equal(found, newObj)) {
                newObj.setAction(WebUtil.ACT_UPDATE);
                actions[index++] = newObj;
            }
        }
        // Set the delete actions
        if (oldData != null) {
            for (DataObject oldObj : oldData) {
                if (oldObj.getAction() == null) { // No mark
                    oldObj.setAction(WebUtil.ACT_DELETE);
                    actions[index++] = oldObj;
                }
            }
        }
        return actions;
    }

    // Compare PK
    private boolean equalPK(DataObject oldObj, DataObject newObj) throws ReflectiveOperationException {
        Class<?> type = oldObj.getClass();
        if (type != newObj.getClass()) {
            throw new RuntimeException("Invalid old and new class");
        }
        String[] pk = oldObj.getPrimaryKey();
        boolean equal = true;
        for (String name : pk) {
            Field f = type.getDeclaredField(name);
            if (!f.get(oldObj).equals(f.get(newObj))) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    // Compare value
    private boolean equal(DataObject oldObj, DataObject newObj) throws ReflectiveOperationException {
        Class<?> type = oldObj.getClass();
        if (type != newObj.getClass()) {
            throw new RuntimeException("Invalid old and new class");
        }
        boolean equal = true;
        Field[] fields = type.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().endsWith("_time")) {
                continue; // Don't compare time
            }
            if (!f.get(oldObj).equals(f.get(newObj))) {
                equal = false;
                break;
            }
        }
        return equal;
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
