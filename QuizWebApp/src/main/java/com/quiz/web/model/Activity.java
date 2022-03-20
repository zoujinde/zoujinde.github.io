package com.quiz.web.model;

public class Activity extends DataObject {

    public int activity_id;
    public int user_id;
    public int event_id;
    public String title;
    public String content;
    public java.sql.Timestamp create_time;

    @Override
    public String getTableName() {
        return "activity";
    }

    @Override
    public String[] getPrimaryKey() {
        return new String[]{"activity_id"};
    }

    @Override
    public String getAutoIdName() {
        return "activity_id";
    }

    @Override
    public void setAutoId(int autoId) {
        activity_id = autoId;
    }

}
