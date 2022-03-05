package com.quiz.web.model;

public class User extends DataObject {

    public int user_id;
    public int parent_id;
    public int user_type;
    public String user_name;
    public String password;
    public String nickname;
    public int birth_year;
    public int gender;
    public String address;
    public String email;
    public String phone;
    public String token;
    public java.sql.Timestamp create_time;
    public java.sql.Timestamp signin_time;

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
    public void setAutoId(int autoId) {
        user_id = autoId;
    }

}
