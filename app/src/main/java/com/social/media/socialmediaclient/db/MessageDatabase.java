package com.social.media.socialmediaclient.db;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.social.media.socialmediaclient.dao.DaoAccess;
import com.social.media.socialmediaclient.model.Message;

@Database(entities = {Message.class}, version = 2, exportSchema = false)
public abstract class MessageDatabase extends RoomDatabase {

    public abstract DaoAccess daoAccess();
}
