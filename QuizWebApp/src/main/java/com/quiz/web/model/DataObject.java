package com.quiz.web.model;

public abstract class DataObject {
    // Get the table name
    public abstract String getTableName();

    // Get the primary key name
    public abstract String[] getPrimaryKey();

    // Get the AutoId name
    public abstract String getAutoIdName();

}
