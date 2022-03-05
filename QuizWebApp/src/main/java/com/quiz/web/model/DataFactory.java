package com.quiz.web.model;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;

public class DataFactory {

    private static final String TAG = DataFactory.class.getSimpleName();
    private static final String TMP_PATH = "D:/tmp_data/";
    private static final String PACKAGE = "com.quiz.web.model";

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
                    + where + " and TABLE_NAME='" + tab + "' order by ordinal_position;";
                ps = cn.prepareStatement(sql);
                rs = ps.executeQuery();
                String name = null;
                String type = null;
                String key  = null;
                String auto = null;
                String cName = tab.substring(0,1).toUpperCase() + tab.substring(1);
                builder.setLength(0);
                builder.append("package ").append(PACKAGE).append(";\n\n");
                builder.append("public class ").append(cName).append(" extends DataObject {\n\n");
                while (rs.next()) {
                    name = rs.getString(1);
                    type = rs.getString(2);
                    builder.append("    public ").append(getClass(type)).append(name).append(";\n");
                    if (rs.getString(3).equals("PRI")) {
                        if (key == null) {
                            key = name;
                        } else {
                            key += "\",\"" + name;
                        }
                    }
                    if (auto == null && rs.getString(4).equals("auto_increment")) {
                        auto = name; // Only get the 1st auto name
                    }
                }
                rs.close();
                ps.close();
                System.out.println("tab=" + tab + " key=" + key + " auto=" + auto);
                buildFooter(builder, tab, key, auto);
                FileWriter fw = new FileWriter(TMP_PATH + cName + ".java");
                fw.write(builder.toString());
                fw.close();
            }
        } catch (Exception e) {
            LogUtil.log(TAG, e.toString());
        } finally {
            WebUtil.close(rs);
            WebUtil.close(ps);
            WebUtil.close(cn);
        }
        System.out.println("END main");
    }

    // Get java class name
    private static String getClass(String type) {
        String name = type.toLowerCase();
        if (name.startsWith("varchar")) {
            name = "String ";
        } else if (name.startsWith("int")) {
            name = "int ";
        } else if (name.startsWith("tinyint")) {
            name = "int ";
        } else if (name.startsWith("timestamp")) {
            name = "java.sql.Timestamp ";
        } else if (name.startsWith("bit")) {
            name = "boolean ";
        } else {
            throw new RuntimeException("Unknown type : " + type);
        }
        return name;
    }

    // Build the class file footer like below
    /*
    @Override
    public String getTableName() {
        return "user";
    }
    @Override
    public String[] getPrimaryKey() {
        return new String[]{"user_id"};
    }
    @Override
    public String getAutoIdName() {
        return "user_id";
    }
     */
    private static void buildFooter(StringBuilder builder, String tab, String key, String auto) {
        builder.append("\n");
        builder.append("    @Override\n");
        builder.append("    public String getTableName() {\n");
        builder.append("        return \"").append(tab).append("\";\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public String[] getPrimaryKey() {\n");
        builder.append("        return new String[]{\"").append(key).append("\"};\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public String getAutoIdName() {\n");
        if (auto == null) {
            builder.append("        return null;\n");
        } else {
            builder.append("        return \"").append(auto).append("\";\n");
        }
        builder.append("    }\n\n");
        builder.append("    @Override\n");
        builder.append("    public void setAutoId(int autoId) {\n");
        if (auto != null) {
            builder.append("        ").append(auto).append(" = autoId;\n");
        }
        builder.append("    }\n\n");
        builder.append("}\n");
    }

}
