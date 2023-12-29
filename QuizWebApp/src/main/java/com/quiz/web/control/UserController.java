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
        /* When user_type is 1 : VOLUNTEER, the return users will only 1 row
        {
          "users":[
            {"user_type":"1","user_name":"Jacking","password":"xxx","email":"","nickname":"",,,,,,"phone":""}
          ]
        }
        */

        /* When user_type is 2 : PARENT, the return users will have 2 or more rows
         * The 1 row is PARENT (user_type is 2)
         * The 2 and more rows are children (user_type is 3)
        {
          "users":[
            {"user_type":"2","user_name":"Guardian", "password":"xxx","email":"","nickname":"",,,,,,"phone":""},
            {"user_type":"3","user_name":"Child 1",  "password":"xxx"},
            {"user_type":"3","user_name":"Child 2",  "password":"xxx"},
          ]
        }
        */

        /* When user_type is 3 : CHILD, the return users will only 1 row, and set token with ParentName
        {
          "users":[
            {"user_type":"3","user_name":"Child-A","password":"xxx", "token":"ParentName",,,,,,"phone":""}
          ]
        }
        */

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
                u.password = WebUtil.SECRET;
                u.token = "";
                User[] children = null;
                if (u.user_type == WebUtil.USER_PARENTS) {
                    children = getChildren(u.user_id);
                } else if (u.user_type == WebUtil.USER_PARTICIPANT) {
                    User[] parent = dm.select(sql, new Object[]{u.parent_id}, User.class);
                    if (parent != null && parent.length == 1) {
                        u.token = "Parent Name : " + parent[0].user_name;
                    }
                }
                // Return the users with children JSON
                StringBuilder s = new StringBuilder(WebUtil.ROWS_LIMIT);
                s.append("{\"users\":[\n");
                s.append(JsonUtil.toJson(u));
                if (children != null && children.length > 0) {
                    for (User child : children) {
                        child.password = WebUtil.SECRET;
                        s.append(",\n");
                        s.append(JsonUtil.toJson(child));
                    }
                }
                s.append("]}");
                result = s.toString();
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
        /* When user_type is 1 : VOLUNTEER, the users only 1 row for UPDATE SQL
        {
          "users":[
            {"user_type":"1","user_name":"Jacking","password":"xxx","email":"","nickname":"",,,,,,"phone":""}
          ]
        }
        */

        /* When user_type is 2 : PARENT, the users will have 1 or more rows. For example:
         * The 1st row is PARENT (user_type is 2), then run UPDATE SQL
         * The 2nd row is a child, user_id > 0, it is an old data, then run UPDATE SQL
         * The 3rd row is a child, user_id = 0, it is  a new data, then run INSERT SQL 
        {
          "users":[
            {"user_type":"2","user_name":"Guardian", "password":"xxx","email":"","nickname":"",,,,,,"phone":""},
            {"user_id":9, "user_type":"3","user_name":"Child 1",  "password":"xxx"},
            {"user_id":0, "user_type":"3","user_name":"Child 2",  "password":"xxx"},
          ]
        }
        */

        /* When user_type is 3 : CHILD, the users will only 1 row for UPDATE SQL
        {
          "users":[
            {"user_type":"3","user_name":"Child-A","password":"xxx",,,,,,"phone":""}
          ]
        }
        */

        String result = WebUtil.OK;
        // Get the users JSON data
        final String[] json = JsonUtil.getArray(body, "users");
        if (json == null || json.length <= 0) {
            result = "Invalid data : users.length <= 0";
        } else {
            try {
                final User[] users = new User[json.length];
                final int userId = WebUtil.getUserId(req);
                final String sql = "select * from user where user_id = ?";
                Object[] args = new Object[]{userId};
                User[] tmp = DataManager.instance().select(sql, args, User.class);
                if (tmp == null || tmp.length != 1) {
                    result = "Invalid user data : " + userId;
                } else {
                    boolean changed = false;
                    // Handle the 1st user data
                    users[0] = tmp[0];
                    // We should only update below columns :
                    String[] items = new String[] {"address", "birth_year", "email", "gender", "nickname", "phone"};
                    if (JsonUtil.setObject(users[0], json[0], items)) { // changed
                        users[0].setAction(WebUtil.ACT_UPDATE);
                        changed = true;
                    }
                    // Check password
                    String pass = JsonUtil.getString(json[0], "password");
                    if (!WebUtil.SECRET.equals(pass)) {
                        pass = LogUtil.encrypt(pass);
                        if (!users[0].password.equals(pass)) {
                            users[0].password = pass;
                            users[0].setAction(WebUtil.ACT_UPDATE);
                            changed = true;
                        }
                    }

                    // If user is parent, then handle children data
                    if (users[0].user_type == WebUtil.USER_PARENTS) {
                        for (int i = 1; i < json.length; i++) {
                            users[i] = new User();
                            JsonUtil.setObject(users[i], json[i], true);
                            String name = users[i].user_name.toLowerCase();
                            if (name.contains(" ")) {
                                result = "Invalid user name : contains space";
                                break;
                            }
                            if (users[i].user_type != WebUtil.USER_PARTICIPANT) {
                                result = "Invalid user type : not PARTICIPANT";
                                break;
                            }
                            if (users[i].user_id < 0) {
                                result = "Invalid user data : id < 0";
                                break;
                            } else if (users[i].user_id == 0) { // Insert new child data
                                users[i].parent_id = userId;
                                setNewUserData(users[i]);
                                changed = true;
                            } else { // Update old child data
                                pass = users[i].password;
                                args = new Object[]{users[i].user_id};
                                tmp = DataManager.instance().select(sql, args, User.class);
                                if (tmp == null || tmp.length != 1) {
                                    result = "No child data : " + users[i].user_id;
                                    break;
                                }
                                users[i] = tmp[0]; // Set user again
                                if (!users[i].user_name.equals(name)) {
                                    users[i].user_name = name;
                                    users[i].setAction(WebUtil.ACT_UPDATE);
                                    changed = true;
                                }
                                if (!WebUtil.SECRET.equals(pass)) {
                                    pass = LogUtil.encrypt(pass);
                                    if (!users[i].password.equals(pass)) {
                                        users[i].password = pass;
                                        users[i].setAction(WebUtil.ACT_UPDATE);
                                        changed = true;
                                    }
                                }
                            }
                        }
                    }
                    // Check the result
                    if (WebUtil.OK.equals(result)) {
                        if (changed) {
                            result = DataManager.instance().runSql(users);
                        } else {
                            result = "User data not changed";
                        }
                    }
                }
            } catch (Exception e) {
                result = "getUser : " + e;
            }
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
                    users[0] = new User();
                    JsonUtil.setObject(users[0], jsonData[0], false);
                    if (users[0].user_type != WebUtil.USER_VOLUNTEER) {
                        result = "Invalid user type : not VOLUNTEER";
                    } else if (users[0].user_name.contains(" ")) {
                        result = "Invalid user name : contains space";
                    }
                } else { // 2 or more rows : must be PARENTS and children
                    for (int i = 0; i < jsonData.length; i++) {
                        users[i] = new User();
                        JsonUtil.setObject(users[i], jsonData[i], true);
                        if (users[i].user_name.contains(" ")) {
                            result = "Invalid user name : contains space";
                            break;
                        } else if (i == 0) { // The 1st row must be parents
                            if (users[i].user_type != WebUtil.USER_PARENTS) {
                                result = "Invalid user type : not PARENTS";
                                break;
                            }
                        } else { // The 2nd and more rows must be children
                            if (users[i].user_type != WebUtil.USER_PARTICIPANT) {
                                result = "Invalid user type : not PARTICIPANT";
                                break;
                            }
                            users[i].parent_id = DataManager.PARENT_ID;
                        }
                    }
                }
                // Run SQL to add users
                if (WebUtil.OK.equals(result)) {
                    for (User u : users) {
                        setNewUserData(u);
                    }
                    result = DataManager.instance().runSql(users);
                }
            } catch (Exception e) {
                result = "addNewUsers : " + e;
            }
        }
        return result;
    }

    // Set new user data
    private void setNewUserData(User u) {
        u.setAction(WebUtil.ACT_INSERT);
        u.user_name = u.user_name.toLowerCase();
        u.create_time = WebUtil.getTime();
        u.signin_time = WebUtil.getTime();
        u.token = "";
        u.password = LogUtil.encrypt(u.password);
    }

}
