package com.quiz.web.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.control.StudentController;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/student")
public class StudentServlet extends HttpServlet {
    private static final String TAG = StudentServlet.class.getSimpleName();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String result = null;
        String act = req.getParameter(WebUtil.ACT);
        if (act == null) {
            LogUtil.println(TAG, "act is null");
        } else if (act.equals(WebUtil.ACT_SELECT)) {
            result = StudentController.instance().getStudent(req);
        } else if (act.equals(WebUtil.ACT_INSERT)) {
            result = StudentController.instance().addStudent(req);
        } else if (act.equals(WebUtil.ACT_UPDATE)) {
            result = StudentController.instance().updateStudent(req);
        } else if (act.equals(WebUtil.ACT_DELETE)) {
            result = StudentController.instance().deleteStudent(req);
        }

        // Write response
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter pw = resp.getWriter();
        pw.println(result);
        pw.flush();
    }

}
