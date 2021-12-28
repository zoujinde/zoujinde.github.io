package com.jinde.web.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.jinde.web.model.DataManager;
import com.jinde.web.model.Student;
import com.jinde.web.util.WebUtil;

public class StudentController {

    private static final StudentController INSTANCE = new StudentController();

    private StudentController() {
    }

    // Single Instance
    public static StudentController instance() {
        return INSTANCE;
    }

    // Get JSON string of student
    public String getStudent(HttpServletRequest req) {
        String sql = "select * from students where id < ?";
        Object[] values = new Object[]{100};
        DataManager dm = DataManager.instance();
        String jsonStr = dm.select(sql, values);
        ArrayList<Student> list = dm.select(sql, values, Student.class);
        for (Student s : list) {
            jsonStr += s.name + "\n";
        }
        return jsonStr;
    }

    // Add a student using below test URL:
    // http://localhost:8080/student?act=insert&name=Adams&gender=true&grace=1&score=100
    public String addStudent(HttpServletRequest req) {
        long autoId = 0;
        Student s = WebUtil.buildObject(req, Student.class);
        if (s != null) {
            autoId = DataManager.instance().insert(new Student[]{s});
        }
        return "AutoId=" + autoId;
    }

    // Update a student
    public String updateStudent(HttpServletRequest req) {
        String result = WebUtil.ACT_UPDATE;
        return result;
    }

    // Delete a student
    public String deleteStudent(HttpServletRequest req) {
        String result = WebUtil.ACT_DELETE;
        return result;
    }

}
