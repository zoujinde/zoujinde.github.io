package com.quiz.web.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Because UserFilter uses : "/*" to block other resources
// So we remark below line : "/"
// @WebServlet(urlPatterns = "/")
public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter pw = resp.getWriter();
        //pw.write("<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">");
        pw.write("<h1>Hello Tomcat</h1>");
        // Don't forget to flush
        pw.flush();
    }

}
