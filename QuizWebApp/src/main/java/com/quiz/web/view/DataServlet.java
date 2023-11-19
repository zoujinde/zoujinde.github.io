package com.quiz.web.view;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.quiz.web.control.DataController;
import com.quiz.web.model.DataManager;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebUtil;

@WebServlet(urlPatterns = "/data")
public class DataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Only define doPost
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result = "UnknownAct";
        String body = WebUtil.getPostBody(req);
        String tab = req.getParameter("tab");
        String act = JsonUtil.getString(body, WebUtil.ACT);
        if (tab == null) {
            result = "tab is null";
        } else if (WebUtil.getUserType(req) != WebUtil.USER_ADMIN) {
            result = "invalid user";
        } else if (body.startsWith("[") && body.endsWith("]")) {
            result = DataController.instance().setData(body, tab);
        } else if (body.startsWith("-- ")) {
            result = DataManager.instance().runSqlScript(body, null);
        } else if (act.equals(WebUtil.ACT_SELECT)) {
            result = DataController.instance().select(body, tab);
        } else if (act.equals(WebUtil.ACT_DELETE)) {
            result = DataController.instance().delete(body, tab);
        } else if (act.equals("show_path")) {
            result = showPath(req);
        } else if (act.equals("dump")) {
            final String file = this.getServletContext().getRealPath("dump.txt");
            LogUtil.log("DataServlet", "dump " + file);
            result = DataManager.instance().dump(file);
        }

        // Write response
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.write(result);
        pw.flush();
    }

    // Show path files
    private String showPath(HttpServletRequest req) {
        StringBuilder builder = new StringBuilder();
        String base = System.getProperty("catalina.base");
        String home = System.getProperty("catalina.home");
        builder.append(base).append(",\n");
        builder.append(home).append(",\n");

        //String path = req.getServletContext().getRealPath("/");
        //String path = getClass().getResource("/").getPath();
        //Check below AWS tomcat path
        buildFiles(builder, new File(home));
        return builder.toString();
    }

    // build files
    private void buildFiles(StringBuilder builder, File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    builder.append(f.getAbsolutePath()).append(",\n");
                    buildFiles(builder, f);
                } else {
                    builder.append(f.getAbsolutePath()).append(",\n");
                }
            }
        }
    }

}
