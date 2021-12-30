package com.jinde.web.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jinde.web.controller.UserController;
import com.jinde.web.util.JsonUtil;
import com.jinde.web.util.LogUtil;
import com.jinde.web.util.WebUtil;

@WebServlet(urlPatterns = "/user")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String TAG = "UserServlet";

    // Only define doPost
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result = "UnknownRequest";
        String body = WebUtil.getPostBody(req);
        String act = JsonUtil.getString(body, WebUtil.ACT);
        //LogUtil.println(TAG, "act=" + act);
        if (act == null) {
            LogUtil.println(TAG, "act is null");
        } else if (act.equals(WebUtil.ACT_SELECT)) {
            result = UserController.instance().select(body);
        } else if (act.equals(WebUtil.ACT_INSERT)) {
            String[] data = JsonUtil.getArray(body, WebUtil.DATA);
            result = UserController.instance().insert(data);
        } else if (act.equals(WebUtil.ACT_UPDATE)) {
            String[] data = JsonUtil.getArray(body, WebUtil.DATA);
            result = UserController.instance().update(data);
        } else if (act.equals(WebUtil.ACT_DELETE)) {
            result = UserController.instance().delete(body);
        }

        // Write response
        //resp.setContentType("text/html");
        //resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.write(result);
        pw.flush();
    }

}
