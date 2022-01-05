package com.jinde.web.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.jinde.web.util.LogUtil;
import com.jinde.web.util.WebUtil;

public class DataFactory {

    private static final String TAG = DataFactory.class.getSimpleName();

    // Main method to create DataObject files from DB
    public static void main(String[] args) throws Exception {
        String url  = "jdbc:mysql://localhost:3306/";
        String user = WebUtil.getValue("jdbc_user");
        String pass = WebUtil.getValue("jdbc_pass");
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String where = " WHERE TABLE_SCHEMA='quiz' ";
        try {
            Class.forName(WebUtil.JDBC_MYSQL);
            cn = DriverManager.getConnection(url, user, pass);
            // Get all tables
            String sql = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES" + where;
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            StringBuilder builder = new StringBuilder();
            while (rs.next()) {
                builder.append(rs.getString(1)).append("\n");
            }
            rs.close();
            ps.close();
            String[] tables = builder.toString().split("\n");
            // Get all columns
            for (String tab : tables) {
                sql = "SELECT COLUMN_NAME,COLUMN_TYPE,COLUMN_KEY,EXTRA FROM INFORMATION_SCHEMA.COLUMNS"
                    + where + " and TABLE_NAME='" + tab + "';";
                ps = cn.prepareStatement(sql);
                rs = ps.executeQuery();
                String name = null;
                String type = null;
                String key  = null;
                String auto = null;
                builder.setLength(0);
                while (rs.next()) {
                    name = rs.getString(1);
                    type = rs.getString(2);
                    System.out.println("tab=" + tab + ", name=" + name + ", type=" + type);
                    if (rs.getString(3).equals("PRI")) {
                        if (key == null) {
                            key = name;
                        } else {
                            key += "," + name;
                        }
                    }
                    if (rs.getString(4).equals("auto_increment")) {
                        auto += name; // Only record 1
                    }
                }
                System.out.println("tab=" + tab + ", key=" + key + ", auto=" + auto);
                rs.close();
                ps.close();
            }
        } catch (Exception e) {
            LogUtil.println(TAG, e.toString());
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
        System.out.println("END main");
    }

    // Create object files
    private void createFiles() {
    }

}
