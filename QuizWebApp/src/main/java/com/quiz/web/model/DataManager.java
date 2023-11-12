package com.quiz.web.model;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebException;
import com.quiz.web.util.WebUtil;

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
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]); // setObject(i + 1, xxx)
            }
            rs = ps.executeQuery();
            // Read lines
            int count = 0;
            T obj = type.newInstance();
            while (rs.next()) {
                obj = buildObject(rs, obj);
                count++;
                if (count > WebUtil.ROWS_LIMIT) {
                    LogUtil.log(TAG, "break on ROWS > " +  WebUtil.ROWS_LIMIT);
                    break;
                }
            }
            if (count > 0) {
                result = (T[])Array.newInstance(type, count);
                for (int i = count - 1; i >= 0; i--) {
                    result[i] = obj;
                    obj = (T) obj.getLast();
                    result[i].setLast(null); // For GC
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw new WebException("select : " + e);
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
        return result;
    }

    // Run SQL select -> Return JSON string
    public String select(String sql, Object[] values) throws WebException {
        StringBuilder builder = new StringBuilder("[ \n");//Must add 1 space;
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
            // Read lines
            int count = 0;
            while (rs.next()) {
                buildJson(rs, builder);
                count++;
                if (count > WebUtil.ROWS_LIMIT) {
                    LogUtil.log(TAG, "break on ROWS > " +  WebUtil.ROWS_LIMIT);
                    break;
                }
            }
            builder.setLength(builder.length() - 2);
            builder.append("\n]\n");
        } catch (Exception e) {
            throw new WebException("select : " + e);
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
        return builder.toString();
    }

    // Run SqlActions one by one
    // Return : result
    public String runSql(DataObject[] actions) {
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
            for (DataObject a : actions) {
                if (a == null) continue; 
                DataObject obj = a;
                String act = a.getAction();
                if (act.equals(WebUtil.ACT_INSERT)) {
                    ps = buildInsertSql(cn, obj, builder, autoId);
                    ps.executeUpdate();
                    if (obj.getAutoIdName() != null) {
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {autoId = rs.getInt(1);}
                        rs.close(); // Must close rs
                        a.setAutoId(autoId); // Set auto ID
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
    private String buildJson(ResultSet rs, StringBuilder builder) throws SQLException {
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
        return builder.toString();
    }

    // Build the object
    private <T extends DataObject> T buildObject(ResultSet rs, T last) throws Exception {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) last.getClass();
        T result = type.newInstance();
        result.setLast(last);
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
    private PreparedStatement buildInsertSql(Connection cn, DataObject obj,
            StringBuilder builder, long autoId) throws SQLException, IllegalAccessException {
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
        String[] pk = obj.getPrimaryKey();
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
                if (autoId > 0 && f.get(obj).equals(0) && name.equals(pk[0])) {
                    ps.setObject(i, autoId);
                } else {
                    ps.setObject(i, f.get(obj));
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
            WebUtil.downloadMySql();
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
    public String dump() {
        StringBuilder builder = new StringBuilder();
        String[] tables = new String[]{
                "user",
                "activity",
                "event",
                "quiz",
                "quiz_item",
                "quiz_result",
        };
        for (String tab : tables) {
            try {
                this.dump(builder, tab);
            } catch (WebException e) {
                builder.setLength(0);
                builder.append(e.toString());
                break;
            }
        }
        return builder.toString();
    }

    // SQL for dump
    private static final String SQL_DUMP = "SELECT COLUMN_NAME " +
            " FROM INFORMATION_SCHEMA.COLUMNS " +
            " WHERE TABLE_SCHEMA='quiz' and TABLE_NAME=? " +
            " order by ordinal_position "; 
 
    // build dump SQL
    private void dump(StringBuilder builder, String table) throws WebException {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = mDataSource.getConnection();
            ps = cn.prepareStatement(SQL_DUMP);
            ps.setObject(1, table);
            rs = ps.executeQuery();
            // Build : insert into table(x,x,x);
            builder.append("insert into ").append(table).append("(");
            int count = 0;
            while (rs.next()) {
                count++;
                if (count > 1) {
                    builder.append(",");
                }
                builder.append(rs.getString(1));
            }
            builder.append("),\n");
            rs.close();
            ps.close();
            // Run select x,x,x from table
            int p1 = builder.lastIndexOf("(");
            int p2 = builder.lastIndexOf(")");
            if (p1 > 0 && p2 > p1) {
                String columns = builder.substring(p1 + 1, p2);
                String select = String.format("select %s from %s", columns, table);
                ps = cn.prepareStatement(select);
                rs = ps.executeQuery();
                ResultSetMetaData data = rs.getMetaData();
                // int count = data.getColumnCount();
                // Build : values(x,x,x);
                int type = 0;
                while (rs.next()) {
                    builder.append("values(");
                    for (int c = 1; c <= count; c++) {
                        if (c > 1) {
                            builder.append(",");
                        }
                        type = data.getColumnType(c);
                        if (type == java.sql.Types.BIGINT) {
                            builder.append(rs.getLong(c));
                        } else if (type == java.sql.Types.TINYINT || type == java.sql.Types.INTEGER) {
                            builder.append(rs.getInt(c));
                        } else if (type == java.sql.Types.BOOLEAN || type == java.sql.Types.BIT) {
                            builder.append(rs.getBoolean(c));
                        } else if (type == java.sql.Types.VARCHAR) {
                            this.build(builder, rs.getString(c));
                        } else if (type == java.sql.Types.TIMESTAMP) {
                            this.build(builder, rs.getTimestamp(c));
                        } else {
                            String log = "invalid types : " + table;
                            LogUtil.log(TAG, log);
                            builder.setLength(0);
                            builder.append(log);
                            break;
                        }
                    }
                    builder.append("),\n");
                }
            } else {
                throw new WebException("invalid columns : " + table);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            throw new WebException("dump : " + e);
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
    }

    // Builder object string
    private void build(StringBuilder builder, Object obj) {
        if (obj == null) {
            builder.append("\"\"");
        } else {
            builder.append("\"").append(obj).append("\"");
        }
    }

}
