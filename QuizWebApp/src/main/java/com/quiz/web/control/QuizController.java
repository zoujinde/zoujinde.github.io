package com.quiz.web.control;

import com.quiz.web.model.DataManager;
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

}
