package com.quiz.web.model;

public class Quiz_item extends DataObject {

    public int quiz_id;
    public int item_id;
    public int item_row;
    public String item_content;
    public int item_type;

    @Override
    public String getTableName() {
        return "quiz_item";
    }

    @Override
    public String[] getPrimaryKey() {
        return new String[]{"quiz_id","item_id","item_row"};
    }

    @Override
    public String getAutoIdName() {
        return null;
    }

    @Override
    public void setAutoId(int autoId) {
    }

}
