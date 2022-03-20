package com.quiz.web.control;

import com.quiz.web.model.DataManager;
import com.quiz.web.util.LogUtil;
import com.quiz.web.util.WebException;

public class EventController {

    private static final String TAG = EventController.class.getSimpleName();

    // Single instance
    private static final EventController INSTANCE = new EventController();
    //private StringBuilder mBuilder = new StringBuilder();

    // Private constructor
    private EventController() {
    }

    // Single instance
    public static EventController instance() {
        return INSTANCE;
    }

    // Get events
    public String getEvents() {
        String result = null;
        try {
            String sql = "select * from event where event_id > ? order by event_id desc";
            result = DataManager.instance().select(sql, new Object[]{1});
        } catch (WebException e) {
            result = "getEvents : " + e.getMessage();
            LogUtil.log(TAG, result);
        }
        return result;
    }

}
