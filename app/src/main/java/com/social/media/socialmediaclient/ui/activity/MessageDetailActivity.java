package com.social.media.socialmediaclient.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.social.media.socialmediaclient.AppConstants;
import com.social.media.socialmediaclient.R;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.util.AppUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MessageDetailActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener,
        View.OnTouchListener , AppConstants {

    private TextView textTime, btnDone, toolbarTitle, MsgTitle, MsgBody;
    private AppCompatImageView MsgImg;
    private ImageView btnDelete;
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        textTime = findViewById(R.id.msg_detail_text_time);
        btnDone = findViewById(R.id.btn_done);
        toolbarTitle = findViewById(R.id.title);
        MsgTitle = findViewById(R.id.msg_title);
        MsgBody = findViewById(R.id.msg_desc);
        MsgImg = findViewById(R.id.msg_detail_image);
        btnDelete = findViewById(R.id.btn_close);
        btnDone = findViewById(R.id.btn_done);

        btnDone.setOnClickListener(this);
        btnDelete.setOnClickListener(this);


        message = (Message) getIntent().getSerializableExtra(INTENT_TASK);
        if(message != null) {
            toolbarTitle.setText(getString(R.string.view_task_title));
            btnDelete.setImageResource(R.drawable.btn_done);
            btnDelete.setTag(R.drawable.btn_done);
            btnDone.setText(R.string.btn_share);
            if(message.getTitle() != null && !message.getTitle().isEmpty()) {
                MsgTitle.setText(message.getTitle());
            }
            if(message.getDescription() != null && !message.getDescription().isEmpty()) {
                MsgBody.setText(message.getDescription());
            }
            if(message.getCreatedAt() != null) {
                textTime.setText(AppUtils.getFormattedDateString(message.getCreatedAt()));
            }
            if(message.getImagestring() != null && !Arrays.toString(message.getImagestring()).isEmpty()) {
                loadImageFromDB();
            }

        }

    }

    @Override
    public void onClick(View view) {
        if(view == btnDelete) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);
        }else if(view == btnDone) {
            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String shareBody = message.getTitle() + "\n" + message.getDescription();
            String shareSub = "New Post message";
            myIntent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(myIntent, "Share using"));

            overridePendingTransition(R.anim.stay, R.anim.slide_down);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    void loadImageFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] bytes = message.getImagestring();
                    // Show Image from DB in ImageView
                    MsgImg.post(new Runnable() {
                        @Override
                        public void run() {
                            MsgImg.setImageBitmap(AppUtils.getImage(bytes));
//                            selectedImageUri = AppUtils.getImageUri(bytes);
                        }
                    });
                } catch (Exception e) {
                    Log.e("Tag", "<loadImageFromDB> Error : " + e.getLocalizedMessage());
                }
            }
        }).start();
    }

}
