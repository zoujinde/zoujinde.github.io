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
                StringBuilder s = new StringBuilder(JsonUtil.SIZE);
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
                // Handle the 1st user data
                final User[] users = new User[json.length];
                final int userId = WebUtil.getUserId(req);
                String[] items = new String[] {"address", "birth_year", "email", "gender", "nickname", "phone"};
                users[0] = this.updateUserData(userId, json[0], items);

                // Handle children data
                items = new String[] {"user_name"};
                for (int i = 1; i < json.length; i++) {
                    String name = JsonUtil.getString(json[i], "user_name");
                    if (name == null || name.contains(" ")) {
                        result = "Invalid user name : " + name;
                        break;
                    }
                    Integer user_type = JsonUtil.getInt(json[i], "user_type");
                    if (user_type == null || user_type != WebUtil.USER_PARTICIPANT) {
                        result = "Invalid user type : not PARTICIPANT";
                        break;
                    }
                    Integer child_id = JsonUtil.getInt(json[i], "user_id");
                    if (child_id == null || child_id < 0) {
                        result = "Invalid child id : " + child_id;
                        break;
                    } else if (child_id == 0) {
                        users[i] = this.insertChildData(json[i], userId);
                    } else {
                        users[i] = this.updateUserData(child_id, json[i], items);
                    }
                }

                // Check the result
                if (WebUtil.OK.equals(result)) {
                    result = DataManager.instance().runSql(users);
                }
            } catch (Exception e) {
                result = "setUser : " + e;
            }
        }
        return result;
    }

    // Insert child data
    private User insertChildData(String json, int parentId) throws Exception {
        User user = new User();
        JsonUtil.setObject(user, json, true);
        user.parent_id = parentId;
        setNewUserData(user);
        return user;
    }

    // Update user data
    private User updateUserData(int userId, String json, String[] items) throws Exception {
        String sql = "select * from user where user_id = ?";
        Object[] args = new Object[]{userId};
        User[] tmp = DataManager.instance().select(sql, args, User.class);
        if (tmp == null || tmp.length != 1) {
            throw new RuntimeException("Can't updateUserData : " + userId);
        }
        User user = tmp[0];
        if (JsonUtil.setObject(user, json, items)) { // changed
            user.setAction(WebUtil.ACT_UPDATE);
            user.user_name = user.user_name.toLowerCase();
        }
        // Check password
        String pass = JsonUtil.getString(json, "password");
        if (!WebUtil.SECRET.equals(pass)) {
            pass = LogUtil.encrypt(pass);
            if (!user.password.equals(pass)) {
                user.setAction(WebUtil.ACT_UPDATE);
                user.password = pass;
            }
        }
        return user;
    }

    // Get request info
    private String getReqId(HttpServletRequest req, User u) {
        StringBuilder s = new StringBuilder(JsonUtil.SIZE);
        s.append(u.user_id).append("#").append(u.user_type).append("#");
        if (u.user_type == WebUtil.USER_ADMIN) {
            s.append("Admin : ");
        } else if (u.user_type == WebUtil.USER_VOLUNTEER) {
            s.append("Volunteer : ");
        } else if (u.user_type== WebUtil.USER_PARENTS) {
            s.append("Parents : ");
        } else if (u.user_type == WebUtil.USER_PARTICIPANT) {
            s.append("Student : ");
        }
        s.append(u.user_name);
        s.append("#").append(req.getRemoteHost());
        s.append("#").append(req.getRemotePort());
        return s.toString();
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
                for (int i = 0; i < jsonData.length; i++) {
                    User u = new User();
                    JsonUtil.setObject(u, jsonData[i], true);
                    // Check the 1st row
                    if (i == 0) {
                        if (users.length == 1 && u.user_type != WebUtil.USER_VOLUNTEER) {
                            result = "Invalid user type : not VOLUNTEER";
                            break;
                        }
                        if (users.length > 1 && u.user_type != WebUtil.USER_PARENTS) {
                            result = "Invalid user type : not PARENTS";
                            break;
                        }
                    } else { // Check the 2nd and more rows
                        if (u.user_type != WebUtil.USER_PARTICIPANT) {
                            result = "Invalid user type : not STUDENT";
                            break;
                        }
                        u.parent_id = DataManager.PARENT_ID;
                    }
                    setNewUserData(u);
                    users[i] = u;
                }
                // Run SQL to add users
                if (WebUtil.OK.equals(result)) {
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
        if (u.user_name.contains(" ")) {
            throw new RuntimeException("Invalid user name : " + u.user_name);
        }
        u.setAction(WebUtil.ACT_INSERT);
        u.user_name = u.user_name.toLowerCase();
        u.create_time = WebUtil.getTime();
        u.signin_time = WebUtil.getTime();
        u.token = "";
        u.password = LogUtil.encrypt(u.password);
    }

    // Delete user data
    public String deleteUser(String body, HttpServletRequest req) {
        String result = WebUtil.OK;
        /* The request body format as below: 
        {"act":"deleteUser", "user_id":123}
        */
        Integer user_id = JsonUtil.getInt(body, "user_id");
        if (user_id == null || user_id <= 0) {
            result = "Can't delete user id : " + user_id;
        } else {
            User u = new User();
            u.user_id = user_id;
            u.setAction(WebUtil.ACT_DELETE);
            result = DataManager.instance().runSql(new User[]{u});
        }
        return result;
    }
}
