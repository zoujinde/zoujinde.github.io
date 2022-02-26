package com.quiz.web.control;

import com.quiz.web.model.DataManager;
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

    // Select user data
    public String select(String body) {
        String result = null;
        Integer[] id = new Integer[]{0,0};
        String error = WebUtil.getIdRange(body, id);
        if (error != null) {
            result = error;
        } else {
            String sql = "select * from user where user_id between ? and ?";
            try {
                result = DataManager.instance().select(sql, id);
            } catch (WebException e) {
                result = e.getMessage();
            }
        }
        return result;
    }

}
