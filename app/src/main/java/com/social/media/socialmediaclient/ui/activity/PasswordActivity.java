package com.social.media.socialmediaclient.ui.activity;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.social.media.socialmediaclient.AppConstants;
import com.social.media.socialmediaclient.R;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.util.AppUtils;
import com.social.media.socialmediaclient.util.NavigatorUtils;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, AppConstants {

    private TextView toolbarTitle, btnDone;
    private ImageView btnClose;
    private EditText editPwd;

    private Message message;
    private boolean pwdVisible;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        toolbarTitle = findViewById(R.id.title);
        toolbarTitle.setText(getString(R.string.toolbar_pwd));

        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);

        btnDone = findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);

        editPwd = findViewById(R.id.edit_pwd);
        editPwd.setOnTouchListener(this);

        message = (Message) getIntent().getSerializableExtra(INTENT_TASK);
        AppUtils.openKeyboard(getApplicationContext());
    }


    private void togglePwd() {
        if(!pwdVisible) {
            pwdVisible = Boolean.TRUE;
            editPwd .setTransformationMethod(null);
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_pwd).mutate();
            drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.line), PorterDuff.Mode.MULTIPLY));
            editPwd.setCompoundDrawablesWithIntrinsicBounds(null,null, drawable, null);

        } else {
            pwdVisible = Boolean.FALSE;
            editPwd.setTransformationMethod(new PasswordTransformationMethod());
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_pwd).mutate();
            drawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
            editPwd.setCompoundDrawablesWithIntrinsicBounds(null,null, drawable, null);
        }
        editPwd.setSelection(editPwd.length());
    }



    @Override
    public void onClick(View view) {
        AppUtils.hideKeyboard(this);

        if(view == btnClose) {
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);

        } else if(view == btnDone) {
            //Evaluate the password
            if(message.getPassword().equals(AppUtils.generateHash(editPwd.getText().toString()))) {
                NavigatorUtils.redirectToEditViewMessageScreen(this, message);

            } else AppUtils.showMessage(getApplicationContext(), getString(R.string.error_pwd));
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;

        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(view.getId() == R.id.edit_pwd && event.getRawX() >= (editPwd.getRight() - editPwd.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                togglePwd();
                return true;
            }
        }
        return false;
    }
}
