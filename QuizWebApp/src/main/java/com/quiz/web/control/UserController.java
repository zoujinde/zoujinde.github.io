package com.quiz.web.control;

import com.quiz.web.model.DataManager;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.WebException;
import com.quiz.web.util.WebUtil;


public class UserController {

    //private static final String TAG = UserController.class.getSimpleName();

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
        String sql = "select user_name from user where user_name=? and password=?";
        try {
            result = DataManager.instance().select(sql, new String[]{user, pass});
            if (result.contains(user)) {
                result = WebUtil.OK;
            }
        } catch (WebException e) {
            result = e.getMessage();
        }
        return result;
    }

    // SignUp
    public String signUp(String body) {
        String result = null;
        return result;
    }

}
