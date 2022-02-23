package com.quiz.web.model;

public class SqlAction {

    public String action = "";
    public String sql = "";
    public Object[] values;
    public DataObject dataObj;

    // Constructor for insert a dataObj
    public SqlAction(DataObject dataObj, String action) {
        this.dataObj = dataObj;
        this.action = action;
    }

    // Constructor for SQL with the values
    public SqlAction(String sql, Object[] values) {
        this.sql = sql;
        this.values = values;
    }

}
