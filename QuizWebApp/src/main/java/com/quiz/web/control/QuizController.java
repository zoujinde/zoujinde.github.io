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
            Quiz_result[] data = DataManager.instance().select(sql, arg, Quiz_result.class, WebUtil.SELECT_FOR_UPDATE);
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
        for (int i = 0; i < json.length; i++) {
            Quiz_result r = new Quiz_result();
            r.quiz_id = quizId;
            r.user_id = userId;
            r.item_row = 0;
            r.answer_time = WebUtil.getTime();
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

}
