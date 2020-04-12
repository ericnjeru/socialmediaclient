package com.social.media.socialmediaclient.repository;


import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.social.media.socialmediaclient.db.MessageDatabase;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.util.AppUtils;

import java.util.List;

public class MessageRepository {

    private String DB_NAME = "db_task";

    private MessageDatabase messageDatabase;
    public MessageRepository(Context context) {
        messageDatabase = Room.databaseBuilder(context, MessageDatabase.class, DB_NAME).build();
    }

    public void insertTask(String title,
                           String description, byte[] imagestring) {

        insertTask(title, description,imagestring, false, null);
    }

    public void insertTask(String title,
                           String description,
                           byte[] imagestring,
                           boolean encrypt,
                           String password) {

        Message message = new Message();
        message.setTitle(title);
        message.setDescription(description);
        message.setImagestring(imagestring);
        message.setCreatedAt(AppUtils.getCurrentDateTime());
        message.setModifiedAt(AppUtils.getCurrentDateTime());
        message.setEncrypt(encrypt);


        if(encrypt) {
            message.setPassword(AppUtils.generateHash(password));
        } else message.setPassword(null);

        insertTask(message);
    }

    private void insertTask(final Message message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                messageDatabase.daoAccess().insertTask(message);
                return null;
            }
        }.execute();
    }

    public void updateTask(final Message message) {
        message.setModifiedAt(AppUtils.getCurrentDateTime());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                messageDatabase.daoAccess().updateTask(message);
                return null;
            }
        }.execute();
    }

    public void deleteTask(final int id) {
        final LiveData<Message> task = getTask(id);
        if(task != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    messageDatabase.daoAccess().deleteTask(task.getValue());
                    return null;
                }
            }.execute();
        }
    }

    public void deleteTask(final Message message) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                messageDatabase.daoAccess().deleteTask(message);
                return null;
            }
        }.execute();
    }

    public LiveData<Message> getTask(int id) {
        return messageDatabase.daoAccess().getTask(id);
    }

    public LiveData<List<Message>> getTasks() {
        return messageDatabase.daoAccess().fetchAllTasks();
    }
}
