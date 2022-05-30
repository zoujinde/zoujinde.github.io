package com.quiz.web.control;

import com.quiz.web.model.DataManager;
import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebException;

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
            sql = "select * from quiz_item where quiz_id = ? order by item_id";
            String item = DataManager.instance().select(sql, new Object[]{quizId});
            sql = "select * from quiz_result where quiz_id = ? and user_id = ? order by item_id";
            result = DataManager.instance().select(sql, new Object[]{quizId, userId});
            StringBuilder s = new StringBuilder();
            JsonUtil.buildJson(s, "title", title);
            JsonUtil.buildJson(s, "quiz_item", item);
            JsonUtil.buildJson(s, "quiz_result", result);
            result = s.toString();
            //LogUtil.log(TAG, result);
        } catch (WebException e) {
            result = "getQuizMainData : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

}
