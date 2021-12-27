package com.jinde.web.controller;

import java.util.ArrayList;

import com.jinde.web.model.DataManager;
import com.jinde.web.model.Student;

public class StudentController {

    private static final StudentController INSTANCE = new StudentController();

    private StudentController() {
    }

    // Single Instance
    public static StudentController getInstance() {
        return INSTANCE;
    }

    // Get JSON string of students
    public String getStudents() {
        String sql = "select * from students where id < ?";
        Object[] values = new Object[]{100};
        String jsonStr = DataManager.select(sql, values);
        ArrayList<Student> list = DataManager.select(sql, values, Student.class);
        for (Student s : list) {
            jsonStr += s.name + "\n";
        }
        return jsonStr;
    }

}
