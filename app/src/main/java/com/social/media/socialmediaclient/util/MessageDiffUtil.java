package com.social.media.socialmediaclient.util;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.social.media.socialmediaclient.model.Message;

import java.util.List;

public class MessageDiffUtil extends DiffUtil.Callback {

    List<Message> oldMessageList;
    List<Message> newMessageList;
    public MessageDiffUtil(List<Message> oldMessageList, List<Message> newMessageList) {
        this.oldMessageList = oldMessageList;
        this.newMessageList = newMessageList;
    }

    @Override
    public int getOldListSize() {
        return oldMessageList.size();
    }

    @Override
    public int getNewListSize() {
        return newMessageList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldMessageList.get(oldItemPosition).getId() == newMessageList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldMessageList.get(oldItemPosition).equals(newMessageList.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
