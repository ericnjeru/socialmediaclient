package com.social.media.socialmediaclient.util;

import android.app.Activity;
import android.content.Intent;

import com.social.media.socialmediaclient.AppConstants;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.ui.activity.AddMessageActivity;
import com.social.media.socialmediaclient.ui.activity.PasswordActivity;

public class NavigatorUtils implements AppConstants {


    public static void redirectToPwdScreen(Activity activity,
                                           Message message) {
        Intent intent = new Intent(activity, PasswordActivity.class);
        intent.putExtra(INTENT_TASK, message);
        activity.startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }


    public static void redirectToEditTaskScreen(Activity activity,
                                               Message message) {
        Intent intent = new Intent(activity, AddMessageActivity.class);
        intent.putExtra(INTENT_TASK, message);
        activity.startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    public static void redirectToViewNoteScreen(Activity activity,
                                                Message message) {
        Intent intent = new Intent(activity, AddMessageActivity.class);
        intent.putExtra(INTENT_TASK, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        activity.startActivity(intent);
        activity.finish();
    }
}
