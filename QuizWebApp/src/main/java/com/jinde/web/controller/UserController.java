package com.jinde.web.controller;

import com.jinde.web.model.DataManager;
import com.jinde.web.model.SqlAction;
import com.jinde.web.model.User;
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
