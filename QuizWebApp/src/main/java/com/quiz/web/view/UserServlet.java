package com.quiz.web.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.control.UserController;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.WebUtil;

@WebServlet(urlPatterns = "/user")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    //private static final String TAG = UserServlet.class.getSimpleName();

    // Only define doPost
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result = "UnknownRequest";
        String body = WebUtil.getPostBody(req);
        String act = JsonUtil.getString(body, WebUtil.ACT);
        //LogUtil.println(TAG, "act=" + act);
        if (act == null) {
            result = "act is null";
        } else if (act.equals("signIn")) {
            result = UserController.instance().signIn(body, req, resp);
        } else if (act.equals("addUser")) {
            result = UserController.instance().addNewUsers(body, req);
        } else if (act.equals("getUser")) {
            result = UserController.instance().getUser(body, req);
        } else if (act.equals("setUser")) {
            result = UserController.instance().setUser(body, req);
        } else if (act.equals("deleteUser")) {
            result = UserController.instance().deleteUser(body, req, resp);
        }

        // Write response
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.write(result);
        pw.flush();
    }

}
