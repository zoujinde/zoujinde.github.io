package com.jinde.web.model;

import java.sql.Timestamp;

public class User extends DataObject {

    public int    user_id;   // INT AUTO_INCREMENT NOT NULL,
    public String user_name; // VARCHAR(30) NOT NULL,
    public String password;  // VARCHAR(20) NOT NULL,
    public String email;     // VARCHAR(30) NOT NULL,
    public String phone;     // VARCHAR(20) NOT NULL,
    public String address;   // VARCHAR(50) NOT NULL,
    public String token;     // VARCHAR(50) NOT NULL,
    public Timestamp create_time; // DATETIME  NOT NULL,

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

    @Override
    public String getSlaveIdName() {
        return null;
    }

}
