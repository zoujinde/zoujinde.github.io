package com.quiz.web.control;

import java.sql.Timestamp;

import com.quiz.web.model.DataManager;
import com.quiz.web.model.Quiz_result;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebException;
import com.quiz.web.util.WebUtil;

public class QuizController {

    private static final String TAG = QuizController.class.getSimpleName();

    // Single instance
    private static final QuizController INSTANCE = new QuizController();
    //private StringBuilder mBuilder = new StringBuilder();

    // Private constructor
    private QuizController() {
    }

    // Single instance
    public static QuizController instance() {
        return INSTANCE;
    }

    // Get quiz main data
    public String getQuizMainData(int userType) {
        String result = null;
        try {
            String sql = "select * from quiz where user_type = ? order by quiz_id";
            result = DataManager.instance().select(sql, new Object[]{userType});
        } catch (WebException e) {
            result = "getQuizMainData : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

    // Get quiz item data
    public String getQuizItemData(int quizId, int userId) {
        String result = null;
        try {
            String sql = "select quiz_name from quiz where quiz_id = ?";
            String title = DataManager.instance().select(sql, new Object[]{quizId});
            sql = "select a.*, b.answer from quiz_item a "
                + "left join quiz_result b "
                + "on a.quiz_id = b.quiz_id and a.item_id = b.item_id and a.item_row = b.item_row and b.user_id = ? "
                + "where a.quiz_id = ? order by a.quiz_id, a.item_id, a.item_row";
            String item = DataManager.instance().select(sql, new Object[]{userId, quizId});
            StringBuilder s = new StringBuilder();
            JsonUtil.buildJson(s, "title", title);
            JsonUtil.buildJson(s, "quiz_item", item);
            result = s.toString();
            //LogUtil.log(TAG, result);
        } catch (Exception e) {
            result = "getQuizMainData : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

    // Set quiz data
    public String setQuizData(int userId, String body) {
        String result = null;
        try {
            int quizId = JsonUtil.getInt(body, "quiz_id");
            String[] json = JsonUtil.getArray(body, "data");
            String sql = "select * from quiz_result where quiz_id = ? and user_id = ?";
            Object[] arg = new Object[]{quizId, userId};
            Quiz_result[] data = DataManager.instance().selectForUpdate(sql, arg, Quiz_result.class);
            // DataObject[] actions = DataManager.instance().getActions(oldData, newData);
            if (data == null) {
                data = new Quiz_result[json.length];
                insertData(data, json, quizId, userId);
            } else {
                updateData(data, json);
            }
            result = DataManager.instance().runSql(data);
        } catch (Exception e) {
            // e.printStackTrace();
            result = "setQuizData : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

    // Insert data
    private void insertData(Quiz_result[] data, String[] json, int quizId, int userId) throws ReflectiveOperationException {
        String[] items = new String[]{"item_id", "answer"};
        Timestamp time = WebUtil.getTime();
        for (int i = 0; i < json.length; i++) {
            Quiz_result r = new Quiz_result();
            r.quiz_id = quizId;
            r.user_id = userId;
            r.item_row = 0;
            r.answer_time = time;
            r.setAction(WebUtil.ACT_INSERT);
            JsonUtil.setObject(r, json[i], items);
            data[i] = r;
        }
    }

    // Update data
    private void updateData(Quiz_result[] data, String[] json) {
        /* Each JSON row only has 2 items : item_id and answer. For example:
         [
           {"item_id":1, "answer":"Yes"},
           {"item_id":2, "answer":"1,3,5"},
         ]
         */
        Timestamp time = WebUtil.getTime();
        for (String s : json) {
            Integer item_id = JsonUtil.getInt(s, "item_id");
            String  answer  = JsonUtil.getString(s, "answer");
            if (item_id != null && answer != null) {
                for (Quiz_result r : data) {
                    if (r.item_id == item_id) {
                        if (!answer.equals(r.answer)) {
                            r.answer = answer;
                            r.answer_time = time;
                            r.setAction(WebUtil.ACT_UPDATE);
                        }
                        break;
                    }
                }
            }
        }
    }

    // Insert quiz by quiz CSV file
    public String insertQuizByCSV(String fileName) {
        String result = WebUtil.OK;
        try {
            // The file name like : quiz_101.csv
            if (fileName.startsWith("quiz_") && fileName.endsWith(".csv") && fileName.length() == 12) {
                int quiz_id = Integer.parseInt(fileName.substring(5, 8));
                // Check quiz result in DB
                String sql = "select * from quiz_result where quiz_id = ? limit 1";
                Object[] values = new Object[]{quiz_id};
                String data = DataManager.instance().select(sql, values);
                if (data.contains("answer")) {
                    result = "Quiz data exist, cannot update quiz_id : " + quiz_id;
                } else { // Read the file content in WEB-INF
                    String content = WebUtil.readFile(WebUtil.getWebInfPath() + fileName);
                    if (content != null) {
                        String[] lines = content.split("\n");
                        result = this.insertByLines(lines, quiz_id);
                    } else {
                        result = "Invalid quiz file content : " + fileName;
                    }
                }
            } else {
                result = "Invalid quiz file name : " + fileName;
            }
        } catch (Exception e) {
            result = "insertQuizByCSV : " + e.getMessage();
        }
        return result;
    }

    // Create SQL scripts according to CSV file lines
    private String insertByLines(String[] lines, int quiz_id) {
        if (lines.length < 6) {
            return "Invalid lines.length : " + lines.length;
        }
        /* The lines like below:
quiz_id,101
quiz_name,Autism Spectrum Screening Questionnaire (ASSQ)
"user_type (1 Volunteer, 2 Parent, 3 Student)",2
,
item_id,1
"item_type (0 single, 1 multiple, 2 text)",0
item_question,is old-fashioned or precocious
option 1,No
option 2,Somewhat
option 3,Yes
,
item_id,2
"item_type (0 single, 1 multiple, 2 text)",0
item_question,is regarded as an eccentric-professor by the other children
option 1,No
option 2,Somewhat
option 3,Yes
,
item_id,3
"item_type (0 single, 1 multiple, 2 text)",0
item_question,lives somewhat in a world of his/her own with restricted idiosyncratic intellectual interests
option 1,No
option 2,Somewhat
option 3,Yes
        */

        // Check quiz_id
        if (lines[0].startsWith("quiz_id")) {
            String quiz_str = getValueInLine(lines[1]);
            if (!quiz_str.equals(quiz_id)) {
                return "Invalid quiz_id value in line : " + lines[0];
            }
        } else {
            return "Invalid quiz_id line : " + lines[0];
        }

        // Check quiz_name
        String quiz_name = null;
        if (lines[1].startsWith("quiz_name")) {
            quiz_name = getValueInLine(lines[1]);
            if (quiz_name.length() > 100) {
                return "Invalid quiz_name length : " + lines[1];
            }
        } else {
            return "Invalid quiz_name line : " + lines[1];
        }

        // Check user_type
        String user_type = null;
        if (lines[2].startsWith("\"user_type")) {
            user_type = getValueInLine(lines[2]);
            if (!user_type.equals("1") && !user_type.equals("2") && !user_type.equals("3")) {
                return "Invalid user_type value in line : " + lines[2];
            }
        } else {
            return "Invalid user_type line : " + lines[2];
        }

        // Create SQL
        String time = "";
        StringBuilder sql = new StringBuilder(lines.length * 20);
        sql.append("delete from quiz_item where quiz_id = ").append(quiz_id).append(";\n");
        sql.append("delete from quiz where quiz_id = ").append(quiz_id).append(";\n");
        sql.append("insert into quiz(quiz_id, quiz_name, user_type, create_time) ");
        sql.append("values(").append(quiz_id).append(",\"").append(quiz_name).append("\",");
        sql.append(user_type).append(",\"").append(time).append("\");\n");

        // Create items SQL
        int item_id = 1;
        String value = null;
        for (String line : lines) {
            if (line.startsWith("item_id")) {
                value = getValueInLine("item_id");
                if (value.equals(item_id)) {
                }
            }
        }
        return WebUtil.OK;
    }

    // Get the 2nd value in CSV line
    private String getValueInLine(String line) {
        String value = null;
        if (line.startsWith("\"")) {
            // For example : "user_type (1 Volunteer, 2 Parent, 3 Student)",2
            int p = line.indexOf("\",");
            if (p > 0) {
                value = line.substring(p + 2).trim();
            }
        } else {
            // For example : quiz_id,101
            int p = line.indexOf(",");
            if (p > 0) {
                value = line.substring(p + 1);
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1).trim();
                }
            }
        }
        if (value == null) {
            throw new RuntimeException("Can't get value  in line : " + line);
        }
        if (value.contains("\"")) {
            throw new RuntimeException("Can't contain \" in line : " + line);
        }
        return value;
    }

}
