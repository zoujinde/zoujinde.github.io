package com.quiz.web.model;

public class Quiz_result extends DataObject {

    public int quiz_id;
    public int item_id;
    public int user_id;
    public String answer;
    public java.sql.Timestamp answer_time;

    @Override
    public String getTableName() {
        return "quiz_result";
    }

    @Override
    public String[] getPrimaryKey() {
        return new String[]{"quiz_id","item_id","user_id"};
    }

    @Override
    public String getAutoIdName() {
        return null;
    }

}
