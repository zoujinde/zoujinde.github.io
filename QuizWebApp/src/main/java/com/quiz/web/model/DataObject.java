package com.quiz.web.model;

import java.lang.reflect.Field;

import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.WebUtil;

public class DataObject {

    private String mSqlAction = null;
    private Object[] mSqlValues = null;
    private boolean mParentObject = true;
    private StringBuilder mUpdateItems = null;

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

    // Check if the item is for building UPDATE SQL
    public boolean isUpdate(String item) {
        boolean result = false;
        if (this.mUpdateItems == null) {
            if (WebUtil.ACT_UPDATE.equals(mSqlAction)) {
                result = true;
            }
        } else {
            int p1 = this.mUpdateItems.indexOf(item);
            int p2 = p1 + item.length();
            if (p1 > 0 && mUpdateItems.charAt(p1 - 1) == ',' && mUpdateItems.charAt(p2) == ',') {
                result = true;
            }
        }
        return result;
    }

    // The update items such as : ,user_name,password,email,
    // We can build UPDATE SQL according to the items
    public void update(String item, Object value) {
        if (this.mUpdateItems == null) {
            this.mUpdateItems = new StringBuilder(JsonUtil.SIZE);
            this.mUpdateItems.append(",");
        }
        if (isUpdate(item)) {
            throw new RuntimeException("Can't update again : " + item);
        } else {
            try {
                Field f = getClass().getField(item);
                f.set(this, value);
                this.mUpdateItems.append(item).append(",");
                this.setAction(WebUtil.ACT_UPDATE);
            } catch (Exception e) {
                throw new RuntimeException("Can't update " + item + " : " + e);
            }
        }
    }

    // Update items by JSON string
    public boolean updateItems(String[] items, String jsonStr) throws ReflectiveOperationException {
        boolean changed = false;
        Class<?> objClass = this.getClass();
        // After the loop, the items will only remain the changed items.
        // Those not changed items will be null.
        // Then we can build the UPDATE SQL according to the changed items.
        for (int i = 0; i < items.length; i++) {
            String name = items[i];
            Object value = null;
            Field f = objClass.getField(name);
            Class<?> type = f.getType();
            // Check long to avoid integer exception
            if (type == Long.class || type == long.class) {
                value = JsonUtil.getLong(jsonStr, name);
            } else if (type == Integer.class || type == int.class) {
                value = JsonUtil.getInt(jsonStr, name);
            } else if (type == Float.class || type == float.class) {
                value = JsonUtil.getFloat(jsonStr, name);
            } else if (type == Double.class || type == double.class) {
                value = JsonUtil.getDouble(jsonStr, name);
            } else if (type == Boolean.class || type == boolean.class) {
                value = JsonUtil.getBoolean(jsonStr, name);
            } else if (type == String.class) {
                value = JsonUtil.getString(jsonStr, name);
            } else if (type == java.sql.Timestamp.class) {
                value = JsonUtil.getTimestamp(jsonStr, name);
            } else {
                throw new RuntimeException("setObject : " + name + " : unknown type : " + type);
            }
            // Check value
            if (value != null && !value.equals(f.get(this))) {
                this.update(name, value);
                changed = true;
            }
        }
        return changed;
    }

}
