package com.social.media.socialmediaclient.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.social.media.socialmediaclient.AppConstants;
import com.social.media.socialmediaclient.BuildConfig;
import com.social.media.socialmediaclient.R;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.util.AppUtils;
import com.social.media.socialmediaclient.util.NavigatorUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

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
            final String sharetitle = message.getTitle();
            final String shareBody = message.getTitle() + "\n" + message.getDescription();

            File imagePath = new File(getApplicationContext().getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            final Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.social.media.socialmediaclient.fileprovider", newFile);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View layout = LayoutInflater.from(this).inflate(R.layout.dialog_item_action, null);
            builder.setView(layout);
            final com.an.customfontview.CustomTextView view_Action = layout.findViewById(R.id.view_action);
            final com.an.customfontview.CustomTextView edit_Action = layout.findViewById(R.id.edit_action);
//            builder.setPositiveButton("ok ", null);
            edit_Action.setText("Email Share");
            view_Action.setText("Twitter Share");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();

            edit_Action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contentUri != null) {
                        Intent shareIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", "", null));
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {});
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, sharetitle);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                        List<ResolveInfo> resolveInfos = getApplicationContext().getPackageManager().queryIntentActivities(shareIntent, 0);
                        if (resolveInfos.size() == 0) {
                            Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        } else {
                            String packageName = resolveInfos.get(0).activityInfo.packageName;
                            String name = resolveInfos.get(0).activityInfo.name;

                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setComponent(new ComponentName(packageName, name));

                            getApplicationContext().startActivity(shareIntent);
                        }

                    }
                }
            });

            view_Action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contentUri != null) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //  permission for receiving app to read this file
                        shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, sharetitle);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                        PackageManager packManager = getPackageManager();
                        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);

                        boolean resolved = false;
                        for (ResolveInfo resolveInfo : resolvedInfoList) {
                            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                                shareIntent.setClassName(
                                        resolveInfo.activityInfo.packageName,
                                        resolveInfo.activityInfo.name);
                                resolved = true;
                                break;
                            }
                        }
                        if (resolved) {
                            startActivity(shareIntent);
                        } else {
                            Intent i = new Intent();
                            i.putExtra(Intent.EXTRA_TEXT, message);
                            i.setAction(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://twitter.com/intent/tweet?text="));
                            startActivity(i);
                            Toast.makeText(getApplicationContext(), "Twitter app isn't found", Toast.LENGTH_LONG).show();
                        }


                        startActivity(Intent.createChooser(shareIntent, "Upload to Twitter"));
                    }
                }
            });


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
                            try {
                                Drawable mDrawable = MsgImg.getDrawable();
                                Bitmap bitmap = ((BitmapDrawable)mDrawable).getBitmap();
                                File cachePath = new File(getApplicationContext().getCacheDir(), "images");
                                cachePath.mkdirs();
                                FileOutputStream stream = new FileOutputStream(cachePath + "/image.png");
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                stream.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("Tag", "<loadImageFromDB> Error : " + e.getLocalizedMessage());
                }
            }
        }).start();
    }



}
