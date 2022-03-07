package com.quiz.web.control;

import javax.servlet.http.HttpServletRequest;

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
    public String signIn(String body, HttpServletRequest req) {
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
                    u.token = WebUtil.getToken(req);
                }
                result = DataManager.instance().runSql(users);
                if (WebUtil.OK.equals(result)) {
                    User u = users[0];
                    req.setAttribute(WebUtil.REQ_ID, WebUtil.getReqId(req, u.user_id, u.user_type));
                    req.setAttribute(WebUtil.REQ_USER, WebUtil.getReqUser(u.user_type, u.user_name));
                }
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
    public String signUp(String body, HttpServletRequest req) {
        String result = WebUtil.OK;
        User user = new User();
        try {
            JsonUtil.toObject(body, user);
            if (user.user_type >= WebUtil.USER_ADMIN && user.user_type <= WebUtil.USER_PARENTS) {
                if (user.parent_id != 0) {
                    result = "Invalid parent id for user <= parents";
                }
            } else if (user.user_type == WebUtil.USER_PARTICIPANT) {
                if (user.birth_year < 1900 || user.birth_year > 2100) {
                    result = "Invalid birth year for participant";
                } else {
                    String[] array = getReqArray(body);
                    int userId = getUserId(array);
                    int userType = getUserType(array);
                    if (userId > 0 && userType == WebUtil.USER_PARENTS) {
                        user.parent_id = userId;
                    } else {
                        result = "Invalid parent id or type for participant";
                    }
                }
            } else { 
                result = "Invalid user type";
            }
            if (result.equals(WebUtil.OK)) {
                user.setAction(WebUtil.ACT_INSERT);
                user.user_name = user.user_name.toLowerCase();
                user.create_time = WebUtil.getTime();
                user.signin_time = WebUtil.getTime();
                user.token = "";
                result = DataManager.instance().runSql(new User[]{user});
            }
        } catch (Exception e) {
            result = "signUp : " + e;
        }
        return result;
    }

    // Get array from reqId like : user_id#user_type#host#port
    private String[] getReqArray(String body) {
        String[] array = null;
        String reqId = JsonUtil.getString(body, WebUtil.REQ_ID);
        if (reqId != null) {
            array = reqId.split("#");
        }
        return array;
    }

    // Get user id
    private int getUserId(String[] array) {
        int id = -1;
        if (array != null && array.length >= 4) {
            id = Integer.parseInt(array[0]);
        }
        return id;
    }

    // Get user type
    private int getUserType(String[] array) {
        int type = -1;
        if (array != null && array.length >= 4) {
            type = Integer.parseInt(array[1]);
        }
        return type;
    }

}
