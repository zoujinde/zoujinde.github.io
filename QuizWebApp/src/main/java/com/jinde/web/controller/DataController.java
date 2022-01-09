package com.jinde.web.controller;

import com.jinde.web.model.DataManager;
import com.jinde.web.model.DataObject;
import com.jinde.web.model.SqlAction;
import com.jinde.web.util.JsonUtil;
import com.jinde.web.util.LogUtil;
import com.jinde.web.util.WebUtil;


public class DataController {

    private static final String TAG = DataController.class.getSimpleName();

    // Single instance
    private static final DataController INSTANCE = new DataController();

    // Private constructor
    private DataController() {
        LogUtil.println(TAG, "private constructor");
    }

    // Single instance
    public static DataController instance() {
        return INSTANCE;
    }

    // Select user data
    public String select(String body, String tab) {
        if (tab.equals("quiz_result")) {
            Integer quiz_id = JsonUtil.getInt(body, "quiz_id");
            Integer user_id = JsonUtil.getInt(body, "user_id");
            //LogUtil.println(TAG, "select : " + quiz_id + " " + user_id);
            if (quiz_id != null && user_id != null) {
                return getQuizResult(quiz_id, user_id);
            }
        }
        String result = null;
        Integer[] id = new Integer[]{0,0};
        String error = WebUtil.getIdRange(body, id);
        if (error != null) {
            result = error;
        } else {
            try {
                Class<?> type = getType(tab);
                DataObject obj = (DataObject) WebUtil.buildObject(type);
                String tabName = obj.getTableName();
                String idName = obj.getPrimaryKey()[0];
                String sql = "select * from " + tabName + " where " + idName + " between ? and ?";
                result = DataManager.instance().select(sql, id);
                if (result.equals("[\n]\n")) {
                    result = "No Result";
                }
            } catch (Exception e) {
                result = e.toString();
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
            try {
                Class<?> type = getType(tab);
                DataObject obj = (DataObject) WebUtil.buildObject(type);
                String tabName = obj.getTableName();
                String idName = obj.getPrimaryKey()[0];
                String sql = "delete from " + tabName + " where " + idName + " between ? and ?";
                SqlAction[] actions = new SqlAction[]{new SqlAction(sql, id)};
                result = DataManager.instance().runSql(actions);
            } catch (Exception e) {
                result = e.toString();
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
            try {
                Class<?> type = getType(tab);
                SqlAction[] act = new SqlAction[data.length];
                DataObject obj = null;
                for (int i = 0; i < data.length; i++) {
                    obj = (DataObject) WebUtil.buildObject(data[i], type);
                    act[i] = new SqlAction(obj, WebUtil.ACT_INSERT);
                }
                result = DataManager.instance().runSql(act);
            } catch (Exception e) {
                result = e.toString();
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
            try {
                Class<?> type = getType(tab);
                SqlAction[] act = new SqlAction[data.length];
                DataObject obj = null;
                for (int i = 0; i < data.length; i++) {
                    obj = (DataObject) WebUtil.buildObject(data[i], type);
                    act[i] = new SqlAction(obj, WebUtil.ACT_UPDATE);
                }
                result = DataManager.instance().runSql(act);
            } catch (Exception e) {
                result = e.toString();
            }
        }
        return result;
    }

    // Set data : insert/update/delete
    public String setData(String body, String tab) {
        String result = null;
        String[] data = JsonUtil.getArray(body);
        if (data == null) {
            result = "No data";
        } else {
            try {
                Class<?> type = getType(tab);
                SqlAction[] act = new SqlAction[data.length];
                DataObject obj = null;
                String action = null;
                for (int i = 0; i < data.length; i++) {
                    action = JsonUtil.getString(data[i], WebUtil.ACT);
                    if (action == null) {
                        result = "Invalid data : act is null";
                        break;
                    }
                    obj = (DataObject) WebUtil.buildObject(data[i], type);
                    act[i] = new SqlAction(obj, action);
                }
                if (result == null) {
                    result = DataManager.instance().runSql(act);
                }
            } catch (Exception e) {
                result = e.toString();
            }
        }
        return result;
    }

    // Get DataObject class type
    private Class<?> getType(String tab) throws ClassNotFoundException {
        String name = "com.jinde.web.model." + tab.substring(0, 1).toUpperCase() + tab.substring(1);
        return Class.forName(name);
    }

    // Get quiz_result by quiz_id and user_id
    public String getQuizResult(int quiz_id, int user_id) {
        String result = null;
        String sql = "select a.quiz_id, a.item_id, a.item_content, a.item_answer,"
                + " a.multi_select, b.user_id, b.answer"
                + " from (select * from quiz_item where quiz_id=?) a"
                + " left join (select * from quiz_result where quiz_id=? and user_id=?) b"
                + " on a.quiz_id = b.quiz_id and a.item_id = b.item_id"
                + " order by a.quiz_id, a.item_id";
        try {
            result = DataManager.instance().select(sql, new Object[]{quiz_id, quiz_id, user_id});
        } catch (Exception e) {
            result = "getQuizResult : " + e;
        }
        return result;
    }

}
