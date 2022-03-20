package com.quiz.web.control;

import com.quiz.web.model.DataManager;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebException;

public class ActivityController {

    private static final String TAG = ActivityController.class.getSimpleName();

    // Single instance
    private static final ActivityController INSTANCE = new ActivityController();
    //private StringBuilder mBuilder = new StringBuilder();

    // Private constructor
    private ActivityController() {
    }

    // Single instance
    public static ActivityController instance() {
        return INSTANCE;
    }

    // Get activities
    public String getActivities(int userId) {
        String result = null;
        try {
            String sql = "select * from activity where user_id = ? order by activity_id desc";
            result = DataManager.instance().select(sql, new Object[]{userId});
        } catch (WebException e) {
            result = "getActivities : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

}
