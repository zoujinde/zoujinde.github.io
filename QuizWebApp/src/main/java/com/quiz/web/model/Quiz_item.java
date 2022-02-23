package com.quiz.web.model;

public class Quiz_item extends DataObject {

    public int quiz_id;
    public int item_id;
    public String item_content;
    public String item_answer;
    public boolean multi_select;

    @Override
    public String getTableName() {
        return "quiz_item";
    }

    @Override
    public String[] getPrimaryKey() {
        return new String[]{"quiz_id","item_id"};
    }

    @Override
    public String getAutoIdName() {
        return null;
    }

}
