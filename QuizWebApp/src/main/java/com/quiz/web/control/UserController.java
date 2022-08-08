package com.quiz.web.control;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.model.DataManager;
import com.quiz.web.model.DataObject;
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
                /* 2022-8-5 Remove the year checking
                 * } else if (user.birth_year < 1900 || user.birth_year > 2100) {
                 *   result = "Invalid birth year for participant";
                 */
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

    // Get user data
    public String getUser(String body, HttpServletRequest req) {
        String result = WebUtil.OK;
        try {
            int userId = JsonUtil.getInt(body, "user_id");
            if (userId == 0) { // Get current user data
                userId = WebUtil.getUserId(req);
            }
            DataManager dm = DataManager.instance();
            String sql = "select * from user where user_id = ?";
            Object[] values = new Object[]{userId};
            User[] users = dm.select(sql, values, User.class);
            if (users != null && users.length == 1) {
                User u = users[0];
                u.password = WebUtil.SECRET_DATA;
                u.token = "";
                if (u.user_type == WebUtil.USER_PARENTS) {
                    users = getChildren(u.user_id);
                    if (users != null) {
                        for (User child : users) {
                            if (u.token.length() == 0) {
                                u.token += " Your child : " + child.user_name;
                            } else {
                                u.token += " , " + child.user_name;
                            }
                        }
                    }
                    u.token = "You are a parent. " + u.token;
                } else if (u.user_type == WebUtil.USER_PARTICIPANT) {
                    u.token = "You are a participant. Your parent : ";
                    users = dm.select(sql, new Object[]{u.parent_id}, User.class);
                    if (users != null && users.length == 1) {
                        u.token += users[0].user_name;
                    }
                } else {
                    u.token = "You are a volunteer.";
                }
                result = JsonUtil.toJson(u);
            } else {
                result = "Invalid users data";
            }
        } catch (Exception e) {
            result = "getUser : " + e;
        }
        return result;
    }

    // Set current user data
    public String setUser(String body, HttpServletRequest req) {
        String result = WebUtil.OK;
        try {
            int userId = WebUtil.getUserId(req);
            String sql = "select * from user where user_id = ?";
            Object[] values = new Object[]{userId};
            DataManager dm = DataManager.instance();
            User[] oldData = dm.select(sql, values, User.class);
            if (oldData != null && oldData.length == 1) {
                User[] newData = new User[]{new User()};
                JsonUtil.toObject(body, newData[0]);
                String[] items = new String[] {"create_time", "parent_id", "signin_time",
                        "token", "user_id", "user_name", "user_type"};
                // Copy some items to remain the old values
                WebUtil.copy(oldData, newData, items);
                if (WebUtil.SECRET_DATA.equals(newData[0].password)) {
                    newData[0].password = oldData[0].password; // remain password
                } else {
                    newData[0].password = LogUtil.encrypt(newData[0].password);
                }
                DataObject[] actions = dm.getActions(oldData, newData);
                result = dm.runSql(actions);
            } else {
                result = "Invalid user data";
            }
        } catch (Exception e) {
            result = "getUser : " + e;
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

    // Get children
    private User[] getChildren(int parentId) throws Exception {
        String sql = "select * from user where parent_id = ?";
        DataManager dm = DataManager.instance();
        User[] users = dm.select(sql, new Object[]{parentId}, User.class);
        return users;
    }

}
