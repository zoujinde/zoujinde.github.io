package com.jinde.web.controller;

import com.jinde.web.model.DataManager;
import com.jinde.web.model.DataObject;
import com.jinde.web.model.SqlAction;
import com.jinde.web.model.User;
import com.jinde.web.util.JsonUtil;
import com.jinde.web.util.LogUtil;
import com.jinde.web.util.WebUtil;


public class DataController {

    private static final String TAG = DataController.class.getSimpleName();
    private static final String UNKNOWN_TYPE = "Unknown type : ";

    // Single instance
    private static final DataController INSTANCE = new DataController();

    // Private constructor
    private DataController() {
    }

    // Single instance
    public static DataController instance() {
        return INSTANCE;
    }

    // Select user data
    public String select(String body, String tab) {
        String result = null;
        Integer[] id = new Integer[]{0,0};
        String error = WebUtil.getIdRange(body, id);
        if (error != null) {
            result = error;
        } else {
            Class<?> type = getType(tab);
            if (type == null) {
                result = UNKNOWN_TYPE + tab;
            } else {
                DataObject obj = (DataObject) WebUtil.buildObject(type);
                String tabName = obj.getTableName();
                String idName = obj.getPrimaryKey()[0];
                String sql = "select * from " + tabName + " where " + idName + " between ? and ?";
                result = DataManager.instance().select(sql, id);
            }
        }
        return result;
    }

    // Delete user data
    public String delete(String body, String tab) {
        String result = null;
        Integer[] id = new Integer[]{0,0};
        String error = WebUtil.getIdRange(body, id);
        if (error != null) {
            result = error;
        } else {
            Class<?> type = getType(tab);
            if (type == null) {
                result = UNKNOWN_TYPE + tab;
            } else {
                DataObject obj = (DataObject) WebUtil.buildObject(type);
                String tabName = obj.getTableName();
                String idName = obj.getPrimaryKey()[0];
                String sql = "delete from " + tabName + " where " + idName + " between ? and ?";
                SqlAction[] actions = new SqlAction[]{new SqlAction(sql, id)};
                result = DataManager.instance().runSql(actions);
            }
        }
        return result;
    }

    // Insert user data
    public String insert(String body, String tab) {
        String result = null;
        String[] data = JsonUtil.getArray(body, WebUtil.DATA);
        if (data == null) {
            result = "No data";
        } else {
            Class<?> type = getType(tab);
            if (type == null) {
                result = UNKNOWN_TYPE + tab;
            } else {
                SqlAction[] act = new SqlAction[data.length];
                DataObject obj = null;
                for (int i = 0; i < data.length; i++) {
                    obj = (DataObject) WebUtil.buildObject(data[i], type);
                    act[i] = new SqlAction(obj, WebUtil.ACT_INSERT);
                }
                result = DataManager.instance().runSql(act);
            }
        }
        return result;
    }

    // Update user data
    public String update(String body, String tab) {
        String result = null;
        String[] data = JsonUtil.getArray(body, WebUtil.DATA);
        if (data == null) {
            result = "No data";
        } else {
            Class<?> type = getType(tab);
            if (type == null) {
                result = UNKNOWN_TYPE + tab;
            } else {
                SqlAction[] act = new SqlAction[data.length];
                DataObject obj = null;
                for (int i = 0; i < data.length; i++) {
                    obj = (DataObject) WebUtil.buildObject(data[i], type);
                    act[i] = new SqlAction(obj, WebUtil.ACT_UPDATE);
                }
                result = DataManager.instance().runSql(act);
            }
        }
        return result;
    }

    // Get DataObject class type
    private Class<?> getType(String tab) {
        Class<?> type = null;
        if (tab.equals("user")) {
            type = User.class;
        } else {
            LogUtil.println(TAG, UNKNOWN_TYPE + tab);
        }
        return type;
    }

}
