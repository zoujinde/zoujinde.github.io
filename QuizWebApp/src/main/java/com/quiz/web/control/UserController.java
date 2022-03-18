package com.quiz.web.control;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.model.DataManager;
import com.quiz.web.model.User;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;


public class UserController {

    private static final String TAG = UserController.class.getSimpleName();

    // Single instance
    private static final UserController INSTANCE = new UserController();
    private StringBuilder mBuilder = new StringBuilder();

    // Private constructor
    private UserController() {
    }

    // Single instance
    public static UserController instance() {
        return INSTANCE;
    }

    // SignIn
    public String signIn(String body, HttpServletRequest req, HttpServletResponse resp) {
        String result = null;
        try {
            String user = JsonUtil.getString(body, "user_name").toLowerCase();
            String pass = JsonUtil.getString(body, "password");
            pass = LogUtil.encrypt(pass);
            //LogUtil.log(TAG, "signIn : " + pass);
            String[] values = new String[]{user, pass};
            String sql = "select * from user where user_name=? and password=?";
            User[] users = DataManager.instance().select(sql, values, User.class);
            if (users != null && users.length == 1) {
                // Update the sign in time and token
                for (User u : users) {
                    u.setAction(WebUtil.ACT_UPDATE);
                    u.signin_time = WebUtil.getTime();
                    //u.token = getToken(req);
                }
                result = DataManager.instance().runSql(users);
                if (WebUtil.OK.equals(result)) {
                    User u = users[0];
                    // Use encrypt to support Chinese etc.
                    String id = LogUtil.encrypt(getReqId(req, u));
                    resp.addCookie(new Cookie(WebUtil.REQ_ID, id));
                    // AJAX can't redirect, so return jsp URL
                    if (u.user_type == WebUtil.USER_ADMIN) {
                        result = "data.jsp";
                    } else {
                        result = "home.jsp";
                    }
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
            if (user.user_type == WebUtil.USER_VOLUNTEER || user.user_type == WebUtil.USER_PARENTS) {
                if (user.parent_id != 0) {
                    result = "Invalid parent id for volunteer or parents";
                }
            } else if (user.user_type == WebUtil.USER_PARTICIPANT) {
                int parentId = WebUtil.getUserId(req);
                if (parentId <= 0) {
                    result = "Invalid parent id for participant";
                } else if (user.birth_year < 1900 || user.birth_year > 2100) {
                    result = "Invalid birth year for participant";
                } else if (WebUtil.getUserType(req) != WebUtil.USER_PARENTS) {
                    result = "Invalid parent type for participant";
                } else {
                    user.parent_id = parentId;
                }
            } else { // Can't add ADMIN by web page
                result = "Invalid user type";
            }
            if (result.equals(WebUtil.OK)) {
                user.setAction(WebUtil.ACT_INSERT);
                user.user_name = user.user_name.toLowerCase();
                user.create_time = WebUtil.getTime();
                user.signin_time = WebUtil.getTime();
                user.token = "";
                user.password = LogUtil.encrypt(user.password);
                //LogUtil.log(TAG, "signUp : " + user.password);
                result = DataManager.instance().runSql(new User[]{user});
            }
        } catch (Exception e) {
            result = "signUp : " + e;
        }
        return result;
    }

    // Get request info
    private synchronized String getReqId(HttpServletRequest req, User u) {
        mBuilder.setLength(0);
        mBuilder.append(u.user_id).append("#").append(u.user_type).append("#");
        if (u.user_type == WebUtil.USER_ADMIN) {
            mBuilder.append("Admin : ");
        } else if (u.user_type == WebUtil.USER_VOLUNTEER) {
            mBuilder.append("Volunteer : ");
        } else if (u.user_type== WebUtil.USER_PARENTS) {
            mBuilder.append("Parents : ");
        } else if (u.user_type == WebUtil.USER_PARTICIPANT) {
            mBuilder.append("Participant : ");
        }
        mBuilder.append(u.user_name);
        mBuilder.append("#").append(req.getRemoteHost());
        mBuilder.append("#").append(req.getRemotePort());
        return mBuilder.toString();
    }

}
