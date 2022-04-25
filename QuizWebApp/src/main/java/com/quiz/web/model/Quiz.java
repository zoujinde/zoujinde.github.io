package com.quiz.web.model;

public class Quiz extends DataObject {

    public int quiz_id;
    public String quiz_name;
    public int user_type;
    public java.sql.Timestamp create_time;

    @Override
    public String getTableName() {
        return "quiz";
    }

    @Override
    public String[] getPrimaryKey() {
        return new String[]{"quiz_id"};
    }

    @Override
    public String getAutoIdName() {
        return "quiz_id";
    }

    @Override
    public void setAutoId(int autoId) {
        quiz_id = autoId;
    }

}
