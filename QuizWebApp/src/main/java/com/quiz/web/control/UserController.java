package com.quiz.web.control;

import com.quiz.web.model.DataManager;
import com.quiz.web.model.User;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;


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

    // SignIn
    public String signIn(String body) {
        String result = null;
        String user = JsonUtil.getString(body, "user_name");
        String pass = JsonUtil.getString(body, "password");
        String[] values = new String[]{user, pass};
        String sql = "select * from user where user_name=? and password=?";
        try {
            User[] users = DataManager.instance().select(sql, values, User.class);
            if (users != null && users.length == 1) {
                // Update the sign in time and token
                users[0].setAction(WebUtil.ACT_UPDATE);
                users[0].signin_time = WebUtil.getTime();
                users[0].token = "123";
                result = DataManager.instance().runSql(users);
            } else {
                result = "Invalid user name or password";
            }
        } catch (Exception e) {
            result = "signIn : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

    // SignUp
    public String signUp(String body) {
        String result = null;
        return result;
    }

}
