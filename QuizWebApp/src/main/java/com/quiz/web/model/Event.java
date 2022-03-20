package com.quiz.web.model;

public class Event extends DataObject {

    public int event_id;
    public int event_type;
    public String title;
    public String content;
    public java.sql.Timestamp create_time;

    @Override
    public String getTableName() {
        return "event";
    }

    @Override
    public String[] getPrimaryKey() {
        return new String[]{"event_id"};
    }

    @Override
    public String getAutoIdName() {
        return "event_id";
    }

    @Override
    public void setAutoId(int autoId) {
        event_id = autoId;
    }

}
