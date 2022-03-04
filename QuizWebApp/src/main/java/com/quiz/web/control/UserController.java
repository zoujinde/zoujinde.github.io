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
    public String signIn(String body, String token) {
        String result = null;
        try {
            String user = JsonUtil.getString(body, "user_name").toLowerCase();
            String pass = JsonUtil.getString(body, "password");
            String[] values = new String[]{user, pass};
            String sql = "select * from user where user_name=? and password=?";
            User[] users = DataManager.instance().select(sql, values, User.class);
            if (users != null && users.length == 1) {
                // Update the sign in time and token
                for (User u : users) {
                    u.setAction(WebUtil.ACT_UPDATE);
                    u.signin_time = WebUtil.getTime();
                    u.token = token;
                }
                result = DataManager.instance().runSql(users);
            } else {
                result = "Invalid user name or password";
            }
        } catch (Exception e) {
            //e.printStackTrace();
            result = "signIn : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

    // SignUp
    public String signUp(String body) {
        String result = WebUtil.OK;
        User user = new User();
        try {
            JsonUtil.toObject(body, user);
            user.setAction(WebUtil.ACT_INSERT);
            user.create_time = WebUtil.getTime();
            user.signin_time = WebUtil.getTime();
            user.token = "";
            if (user.user_type >= WebUtil.USER_ADMIN && user.user_type <= WebUtil.USER_PARENTS) {
                if (user.parent_id != 0) {
                    result = "Invalid parent id for normal user";
                }
            } else if (user.user_type == WebUtil.USER_PARTICIPANT) {
                if (user.parent_id <= 0) {
                    result = "Invalud parent id for participant";
                } else if (user.birth_year < 1900 || user.birth_year > 2100) {
                    result = "Invalid birth year for participant";
                }
            } else { 
                result = "Invalid user type";
            }
            if (result.equals(WebUtil.OK)) {
                user.user_name = user.user_name.toLowerCase();
                result = DataManager.instance().runSql(new User[]{user});
            }
        } catch (Exception e) {
            result = "signUp : " + e;
        }
        return result;
    }

}
