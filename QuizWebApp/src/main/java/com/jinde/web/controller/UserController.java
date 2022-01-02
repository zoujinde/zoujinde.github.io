package com.jinde.web.controller;

import com.jinde.web.model.DataManager;
import com.jinde.web.model.SqlAction;
import com.jinde.web.model.User;
import com.jinde.web.util.JsonUtil;
import com.jinde.web.util.LogUtil;
import com.jinde.web.util.WebUtil;


public class UserController {

    private static final String TAG = UserController.class.getSimpleName();

    // Single instance
    private static final UserController INSTANCE = new UserController();

    // Private constructor
    private UserController() {
    }

    // Single instance
    public static UserController instance() {
        return INSTANCE;
    }

    // Select user data
    public String select(String body) {
        String result = null;
        Integer[] id = new Integer[]{0,0};
        String error = WebUtil.getIdRange(body, id);
        if (error != null) {
            result = error;
        } else {
            String sql = "select * from user where user_id between ? and ?";
            result = DataManager.instance().select(sql, id);
        }
        return result;
    }

    // Delete user data
    public String delete(String body) {
        String result = null;
        Integer[] id = new Integer[]{0,0};
        String error = WebUtil.getIdRange(body, id);
        if (error != null) {
            result = error;
        } else {
            String sql = "delete from user where user_id between ? and ?";
            SqlAction[] actions = new SqlAction[1];
            actions[0] = new SqlAction(sql, id);
            result = DataManager.instance().runSql(actions);
        }
        return result;
    }

    // Insert user data
    public String insert(String body) {
        String[] data = JsonUtil.getArray(body, WebUtil.DATA);
        String result = null;
        if (data == null) {
            result = "No data";
            LogUtil.println(TAG, result);
        } else {
            SqlAction[] act = new SqlAction[data.length];
            User user = null;
            for (int i = 0; i < data.length; i++) {
                user = WebUtil.buildObject(data[i], User.class);
                act[i] = new SqlAction(user, WebUtil.ACT_INSERT);
            }
            result = DataManager.instance().runSql(act);
        }
        return result;
    }

    // Update user data
    public String update(String body) {
        String result = null;
        String[] data = JsonUtil.getArray(body, WebUtil.DATA);
        if (data == null) {
            result = "No data";
        } else {
            SqlAction[] act = new SqlAction[data.length];
            User user = null;
            for (int i = 0; i < data.length; i++) {
                user = WebUtil.buildObject(data[i], User.class);
                act[i] = new SqlAction(user, WebUtil.ACT_UPDATE);
            }
            result = DataManager.instance().runSql(act);
        }
        return result;
    }

}
