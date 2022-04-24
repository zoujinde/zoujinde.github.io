package com.quiz.web.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;

@WebServlet(urlPatterns ={"/quiz"})
public class QuizServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String TAG = QuizServlet.class.getSimpleName();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result = WebUtil.OK;
        String body = WebUtil.getPostBody(req);
        String act = JsonUtil.getString(body, WebUtil.ACT);
        LogUtil.log(TAG, "act=" + act);
        if (act == null) {
            result = "act is null";
        } else if (act.equals("getQuizMain")) {
            result = "[{\"title\":\"Quiz 2022 March\", \"time\":\"2022-03-01\"},";
            result += "{\"title\":\"Quiz 2022 April\", \"time\":\"2022-04-01\"}]";
        }
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.write(result);
        pw.flush();
    }

}
