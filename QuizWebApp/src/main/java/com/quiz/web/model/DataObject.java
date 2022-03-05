package com.quiz.web.model;

public class DataObject {

    private String mSqlAction = null;
    private Object[] mSqlValues = null;
    private DataObject mLast = null;

    // Constructor
    public DataObject() {
    }

    // Get last
    public DataObject getLast() {
        return mLast;
    }

    // Set last
    public void setLast(DataObject obj) {
        mLast = obj;
    }

    // Constructor for SQL with the values
    public DataObject(String sql, Object[] values) {
        this.mSqlAction = sql;
        this.mSqlValues = values;
    }

    // Get action
    public String getAction() {
        return this.mSqlAction;
    }

    // Set action
    public void setAction(String act) {
        this.mSqlAction = act;
    }

    // Get values
    public Object[] getValues() {
        return this.mSqlValues;
    }

    // Get the table name
    public String getTableName(){
        return null;
    }

    // Get the primary key name
    public String[] getPrimaryKey(){
        return null;
    }

    // Get the AutoId name
    public String getAutoIdName(){
        return null;
    }

    // Set the AutoId
    public void setAutoId(int autoId){
    }

}
