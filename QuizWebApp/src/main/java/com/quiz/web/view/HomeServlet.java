package com.quiz.web.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.control.ActivityController;
import com.quiz.web.control.EventController;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;

@WebServlet(urlPatterns ={"/home"})
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String TAG = HomeServlet.class.getSimpleName();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result = WebUtil.OK;
        String body = WebUtil.getPostBody(req);
        String act = JsonUtil.getString(body, WebUtil.ACT);
        LogUtil.log(TAG, "act=" + act);
        if (act == null) {
            result = "act is null";
        } else if (act.equals("getData")) {
            int userId = WebUtil.getUserId(req);
            String events = EventController.instance().getEvents();
            String activities = ActivityController.instance().getActivities(userId);
            String[] keys = new String[]{"events", "activities"};
            String[] values = new String[]{events, activities};
            result = JsonUtil.toJson(keys, values);
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.write(result);
        pw.flush();
    }

}
