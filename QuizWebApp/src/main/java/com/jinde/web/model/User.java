package com.jinde.web.model;

public class User extends DataObject {

    public int user_id;
    public String user_name;
    public String password;
    public String email;
    public String phone;
    public String address;
    public String token;
    public java.sql.Timestamp create_time;

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

}
