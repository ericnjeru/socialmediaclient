package com.social.media.socialmediaclient.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.social.media.socialmediaclient.AppConstants;
import com.social.media.socialmediaclient.R;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.util.AppUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Objects;


public class AddMessageActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener,
                                                                    View.OnTouchListener , AppConstants {

    private EditText editTitle, editDesc, editPwd;
    private TextView textTime, btnDone, toolbarTitle;
    private AppCompatImageView msgImage;
    private AppCompatButton msgAddImg;
    private AppCompatCheckBox checkBox;
    private ImageView btnDelete;

    private Message message;
    private boolean pwdVisible;

    private static final int SELECT_PICTURE = 100;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);

        textTime = findViewById(R.id.text_time);
        toolbarTitle = findViewById(R.id.title);
        editTitle = findViewById(R.id.edit_title);
        editDesc = findViewById(R.id.edit_desc);
        msgImage = findViewById(R.id.msg_image);


        editPwd = findViewById(R.id.edit_pwd);
        editPwd.setOnTouchListener(this);

        checkBox = findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(this);

        msgAddImg = findViewById(R.id.msg_btn);
        msgAddImg.setOnClickListener(this);

        btnDelete = findViewById(R.id.btn_close);
        btnDelete.setOnClickListener(this);

        btnDone = findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);

        message = (Message) getIntent().getSerializableExtra(INTENT_TASK);
        if(message == null) {
            toolbarTitle.setText(getString(R.string.add_task_title));
            btnDelete.setImageResource(R.drawable.btn_done);
            btnDelete.setTag(R.drawable.btn_done);
            msgImage.setImageResource(R.drawable.placeholder);
            textTime.setText(AppUtils.getFormattedDateString(AppUtils.getCurrentDateTime()));

        } else {
            toolbarTitle.setText(getString(R.string.edit_task_title));
            btnDelete.setImageResource(R.drawable.ic_delete);
            btnDelete.setTag(R.drawable.ic_delete);
            if(message.getTitle() != null && !message.getTitle().isEmpty()) {
                editTitle.setText(message.getTitle());
                editTitle.setSelection(editTitle.getText().length());

            }
            if(message.getDescription() != null && !message.getDescription().isEmpty()) {
                editDesc.setText(message.getDescription());
                editDesc.setSelection(editDesc.getText().length());
            }
            if(message.getCreatedAt() != null) {
                textTime.setText(AppUtils.getFormattedDateString(message.getCreatedAt()));
            }
            if(message.getPassword() != null && !message.getPassword().isEmpty()) {
                editPwd.setText(message.getPassword());
                editPwd.setSelection(editPwd.getText().length());
            }
            if(message.getImagestring() != null && !Arrays.toString(message.getImagestring()).isEmpty()) {
                loadImageFromDB();
            }
            checkBox.setChecked(message.isEncrypt());
        }

        AppUtils.openKeyboard(getApplicationContext());
    }



    private void togglePwd() {
        if(!pwdVisible) {
            pwdVisible = Boolean.TRUE;
            editPwd.setTransformationMethod(null);
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_pwd).mutate();
            drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.line), PorterDuff.Mode.MULTIPLY));
            editPwd.setCompoundDrawablesWithIntrinsicBounds(null,null, drawable, null);

        } else {
            pwdVisible = Boolean.FALSE;
            editPwd.setTransformationMethod(new PasswordTransformationMethod());
            Drawable drawable = Objects.requireNonNull(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_pwd)).mutate();
            drawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY));
            editPwd.setCompoundDrawablesWithIntrinsicBounds(null,null, drawable, null);
        }
        editPwd.setSelection(editPwd.length());
    }

    // Choose an image from Gallery
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    msgImage.setImageURI(selectedImageUri);
                    msgImage.setVisibility(View.VISIBLE);
                    msgAddImg.setText("change Image");
                }
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
            editPwd.setVisibility(View.VISIBLE);
            editPwd.setFocusable(true);
        } else {
            editPwd.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        AppUtils.hideKeyboard(this);
        if(view == btnDelete) {

            if((Integer)btnDelete.getTag() == R.drawable.btn_done) {
                setResult(Activity.RESULT_CANCELED);

            } else {
                Intent intent = getIntent();
                intent.putExtra(INTENT_DELETE, true);
                intent.putExtra(INTENT_TASK, message);
                setResult(Activity.RESULT_OK, intent);
            }

            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);

        }
        else if (view == msgAddImg){
            openImageChooser();
        }
        else if(view == btnDone) {
            if (editTitle.getText().toString().isEmpty() || editDesc.getText().toString().isEmpty()){
                Toast.makeText(this, "Please fill all the fields first", Toast.LENGTH_SHORT).show();
                return;
            }
            if(selectedImageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = getIntent();
            if(message != null) {
                message.setTitle(editTitle.getText().toString());
                message.setDescription(editDesc.getText().toString());
                message.setEncrypt(checkBox.isChecked());
                message.setPassword(editPwd.getText().toString());

                //lets insert image now
                InputStream iStream = null;
                byte[] inputData = new byte[0];
                try {
                    if (selectedImageUri != null){
                        iStream = getContentResolver().openInputStream(selectedImageUri);
                        assert iStream != null;
                        inputData = AppUtils.getBytes(iStream);
                        message.setImagestring(inputData);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


                intent.putExtra(INTENT_TASK, message);

            } else {
                InputStream iStream = null;
                byte[] inputData = new byte[0];
                if (selectedImageUri != null){
                    try {
                        iStream = getContentResolver().openInputStream(selectedImageUri);
                        assert iStream != null;
                        inputData = AppUtils.getBytes(iStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                intent.putExtra(INTENT_TITLE, editTitle.getText().toString());
                intent.putExtra(INTENT_DESC, editDesc.getText().toString());
                intent.putExtra(INTENT_IMGBYT, inputData);
                intent.putExtra(INTENT_ENCRYPT, checkBox.isChecked());
                intent.putExtra(INTENT_PWD, editPwd.getText().toString());
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.stay, R.anim.slide_down);
        }
    }

    void loadImageFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] bytes = message.getImagestring();
                    // Show Image from DB in ImageView
                    msgImage.post(new Runnable() {
                        @Override
                        public void run() {
                            msgImage.setImageBitmap(AppUtils.getImage(bytes));
//                            selectedImageUri = AppUtils.getImageUri(bytes);
                        }
                    });
                } catch (Exception e) {
                    Log.e("Tag", "<loadImageFromDB> Error : " + e.getLocalizedMessage());
                }
            }
        }).start();
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
