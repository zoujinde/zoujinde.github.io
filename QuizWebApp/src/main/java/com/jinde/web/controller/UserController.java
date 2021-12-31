package com.jinde.web.controller;

import com.jinde.web.model.DataManager;
import com.jinde.web.model.SqlAction;
import com.jinde.web.model.User;
import com.jinde.web.util.JsonUtil;
import com.jinde.web.util.LogUtil;
import com.jinde.web.util.WebUtil;


public class UserController {

    private static final String TAG = "UserController";

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
        Integer[] id = new Integer[]{0,0};
        String result = getIdRange(body, id);
        if (id[0] > 0 && id[1] > 0) {
            String sql = "select * from user where user_id between ? and ?";
            result = DataManager.instance().select(sql, id);
        }
        return result;
    }

    // Get ID range
    private String getIdRange(String body, Integer[] id) {
        String result = null;
        try {
            String[] range = JsonUtil.getString(body, "user_id_range").split("-");
            Integer id1 = Integer.parseInt(range[0].trim());
            Integer id2 = Integer.parseInt(range[1].trim());
            //LogUtil.println(TAG, id1 + " - " + id2);
            if (id1 > id2 || id1 < 0) {
                result = "Invalid user_id_range";
            } else if (id2 - id1 > 300) {
                result = "Invalid user_id_range > 300";
            } else {
                id[0] = id1;
                id[1] = id2;
            }
        } catch (Exception e) {
            result = e.toString();
        }
        return result;
    }

    // Delete user data
    public String delete(String body) {
        Integer[] id = new Integer[]{0,0};
        String result = getIdRange(body, id);
        if (id[0] > 0 && id[1] > 0) {
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
        } else {
            SqlAction[] act = new SqlAction[data.length];
            User user = null;
            for (int i = 0; i < data.length; i++) {
                user = WebUtil.buildObject(data[i], User.class);
                act[i] = new SqlAction(user, WebUtil.ACT_INSERT);
            }
            result = DataManager.instance().runSql(act);
            LogUtil.println(TAG, result);
        }
        return result;
    }

    // Update user data
    public String update(String body) {
        String[] data = JsonUtil.getArray(body, WebUtil.DATA);
        String result = null;
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
        //LogUtil.println(TAG, result);
        return result;
    }

}
