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
        Integer id1 = JsonUtil.getInt(body, "id1");
        Integer id2 = JsonUtil.getInt(body, "id2");
        //LogUtil.println(TAG, id1 + " - " + id2);
        String result = null;
        if (id1 == null || id2 == null || id1 > id2 || id1 < 0) {
            result = "Invalid id1 or id2";
        } else if (id2 - id1 > 100) {
            result = "Invalid id2 - id1 > 100";
        } else {
            String sql = "select * from user where user_id >= ? and user_id <= ?";
            result = DataManager.instance().select(sql, new Object[]{id1, id2});
        }
        return result;
    }

    // Insert user data
    public String insert(String[] data) {
        SqlAction[] act = new SqlAction[data.length];
        User user = null;
        for (int i = 0; i < data.length; i++) {
            user = WebUtil.buildObject(data[i], User.class);
            act[i] = new SqlAction(user, WebUtil.ACT_INSERT);
        }
        long autoId = DataManager.instance().runSql(act);
        String result = "insert " + autoId;
        LogUtil.println(TAG, result);
        return result;
    }

}
