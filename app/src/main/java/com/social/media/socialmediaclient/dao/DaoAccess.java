package com.social.media.socialmediaclient.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.social.media.socialmediaclient.model.Message;

import java.util.List;

@Dao
public interface DaoAccess {

    @Insert
    Long insertTask(Message message);


    @Query("SELECT * FROM Message ORDER BY created_at desc")
    LiveData<List<Message>> fetchAllTasks();


    @Query("SELECT * FROM Message WHERE id =:taskId")
    LiveData<Message> getTask(int taskId);


    @Update
    void updateTask(Message message);


    @Delete
    void deleteTask(Message message);
}
