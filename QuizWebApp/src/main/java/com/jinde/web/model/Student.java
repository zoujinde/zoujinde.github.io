package com.jinde.web.model;

public class Student implements DataObject {
    public long id;// bigint AI PK 
    public String name;// varchar(50) 
    public boolean gender;// tinyint(1) 
    public int grade;// int 
    public int score;// int

    @Override
    public String getTableName() {
        return "students";
    }

    @Override
    public String getAutoIdName() {
        return "id";
    }

    @Override
    public String getSlaveIdName() {
        return null;
    }

}
