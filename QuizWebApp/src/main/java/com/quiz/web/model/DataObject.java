package com.quiz.web.model;

import java.lang.reflect.Field;

import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;

public class DataObject {

    private String mSqlAction = null;
    private Object[] mSqlValues = null;
    private boolean mParentObject = true;

    // Package inner visible
    DataObject mSelectForUpdate = null;

    // Constructor
    public DataObject() {
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
        if (WebUtil.ACT_UPDATE.equals(act) && mSelectForUpdate == null) {
            throw new RuntimeException("mSelectForUpdate is null");
        }
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

    // isParentObject
    public boolean isParentObject(){
        return mParentObject;
    }

    // setParentObject
    public void setParentObject(boolean state){
        mParentObject = state;
    }

    // Check field changed
    public boolean isChanged(Field field) {
        if (this.mSelectForUpdate == null) {
            throw new RuntimeException("mSelectForUpdate is null");
        }
        boolean changed = false;
        try {
            Object v1 = field.get(this);
            Object v2 = field.get(this.mSelectForUpdate);
            if (v1 == null) {
                changed = (v2 != null);
            } else {
                changed = (!v1.equals(v2));
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            LogUtil.log("DataObject", field.getName() + " : " + e);
        }
        return changed;
    }

}
