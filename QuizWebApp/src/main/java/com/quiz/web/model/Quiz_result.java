package com.quiz.web.model;

public class Quiz_result extends DataObject {

    public int quiz_id;
    public int user_id;
    public int item_id;
    public int item_row;
    public String answer;
    public java.sql.Timestamp answer_time;

    @Override
    public String getTableName() {
        return "quiz_result";
    }

    @Override
    public String[] getPrimaryKey() {
        return new String[]{"quiz_id","user_id","item_id","item_row"};
    }

    @Override
    public String getAutoIdName() {
        return null;
    }

    @Override
    public void setAutoId(int autoId) {
    }

}
