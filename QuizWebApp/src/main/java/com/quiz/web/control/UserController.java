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
            String[] args = new String[]{user, pass};
            String sql = "select * from user where user_name=? and password=?";
            User[] users = DataManager.instance().select(sql, args, User.class);
            if (users != null && users.length == 1) {
                // Update the sign in time and token
                User u = users[0];
                u.setAction(WebUtil.ACT_UPDATE);
                u.signin_time = WebUtil.getTime();
                //u.token = getToken(req);
                result = DataManager.instance().runSql(users);
                if (WebUtil.OK.equals(result)) {
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
            Object[] args = new Object[]{userId};
            User[] users = DataManager.instance().select(sql, args, User.class);
            if (users != null && users.length == 1) {
                User user = users[0];
                // We should only update below columns :
                String[] items = new String[] {"address", "birth_year",
                        "email", "gender", "nickname", "phone"};
                boolean changed = JsonUtil.setObject(user, body, items);
                // Check password
                String pass = JsonUtil.getString(body, "password");
                if (!WebUtil.SECRET_DATA.equals(pass)) {
                    changed = true;
                    user.password = LogUtil.encrypt(pass);
                }
                if (changed) {
                    user.setAction(WebUtil.ACT_UPDATE);
                    result = DataManager.instance().runSql(users);
                } else {
                    result = "User data not changed";
                }
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

    // Add new users
    public String addNewUsers(String body, HttpServletRequest req) {
        String result = WebUtil.OK;

        /* When users array only has 1 row, the user_type must be 1 : VOLUNTEER
        {
          "act":"addUser",
          "users":[
            {"user_type":"1","user_name":"zoujinde","password":"11111111","email":"","nickname":"",,,,,,"phone":""}
          ]
        }
        */

        /* When users array has 2 or more rows,
         * The 1st user_type must be 2          : Guardian PARENTS
         * The 2nd and more user_type must be 3 : Child PARTICIPANT
        {
          "act":"addUser",
          "users":[
            {"user_type":"2","user_name":"Guardian", "password":"xxx","email":"","nickname":"",,,,,,"phone":""},
            {"user_type":"3","user_name":"Child 1",  "password":"xxx"},
            {"user_type":"3","user_name":"Child 2",  "password":"xxx"},
          ]
        }
        */

        // Get the users JSON data
        String[] jsonData = JsonUtil.getArray(body, "users");
        if (jsonData == null || jsonData.length <= 0) {
            result = "Invalid data : users.length <= 0";
        } else { // Save the users data
            try {
                User[] users = new User[jsonData.length];
                if (users.length == 1) { // Only 1 row : must be VOLUNTEER
                    User user = new User();
                    users[0] = user;
                    JsonUtil.setObject(user, jsonData[0], false);
                    if (user.user_type != WebUtil.USER_VOLUNTEER) {
                        result = "Invalid user type : not VOLUNTEER";
                    } else if (user.user_name.contains(" ")) {
                        result = "Invalid user name : contains space";
                    }
                } else { // 2 or more rows : must be PARENTS and children
                    for (int i = 0; i < jsonData.length; i++) {
                        User user = new User();
                        users[i] = user;
                        JsonUtil.setObject(user, jsonData[i], true);
                        if (user.user_name.contains(" ")) {
                            result = "Invalid user name : contains space";
                            break;
                        } else if (i == 0) { // The 1st row must be parents
                            if (user.user_type != WebUtil.USER_PARENTS) {
                                result = "Invalid user type : not PARENTS";
                                break;
                            }
                        } else { // The 2nd and more rows must be children
                            if (user.user_type != WebUtil.USER_PARTICIPANT) {
                                result = "Invalid user type : not PARTICIPANT";
                                break;
                            }
                            user.parent_id = DataManager.PARENT_ID;
                        }
                    }
                }
                // Run SQL to add users
                if (WebUtil.OK.equals(result)) {
                    for (User u : users) {
                        u.setAction(WebUtil.ACT_INSERT);
                        u.user_name = u.user_name.toLowerCase();
                        u.create_time = WebUtil.getTime();
                        u.signin_time = WebUtil.getTime();
                        u.token = "";
                        u.password = LogUtil.encrypt(u.password);
                    }
                    result = DataManager.instance().runSql(users);
                }
            } catch (Exception e) {
                result = "addNewUsers : " + e;
            }
        }
        return result;
    }

}
