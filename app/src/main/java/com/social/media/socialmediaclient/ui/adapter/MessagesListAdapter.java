package com.social.media.socialmediaclient.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Filter;
import android.widget.Filterable;
import com.social.media.socialmediaclient.R;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.util.AppUtils;
import com.social.media.socialmediaclient.util.MessageDiffUtil;

import java.util.ArrayList;
import java.util.List;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.CustomViewHolder>
        implements Filterable{

    private List<Message> messages;
    private List<Message> messageFiltered;
    private Observer<List<Message>> context;
    
    public MessagesListAdapter(Observer<List<Message>> context, List<Message> messages) {

        this.context = context;
        this.messages = messages;
        this.messageFiltered = messages;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Message message = getItem(position);

        holder.itemTitle.setText(message.getTitle());
        holder.itemTime.setText(AppUtils.getFormattedDateString(message.getCreatedAt()));
        loadImageFromDB(message,holder.itemImage);

        if(message.isEncrypt()) {
            holder.itemTime.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_lock, 0);

        } else {
            holder.itemTime.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
        }


    }

    @Override
    public int getItemCount() {
        return messageFiltered.size();
    }

    public Message getItem(int position) {
        return messageFiltered.get(position);
    }

    public void addTasks(List<Message> newMessages) {
        MessageDiffUtil messageDiffUtil = new MessageDiffUtil(messageFiltered, newMessages);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(messageDiffUtil);
        messageFiltered.clear();
        messageFiltered.addAll(newMessages);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    messageFiltered = messages;
                } else {
                    List<Message> filteredList = new ArrayList<>();
                    for (Message row : messages) {

                        // message match condition. this might differ depending on your requirement
                        // here we are looking for message body match or title match
                        if (row.getDescription().toLowerCase().contains(charString.toLowerCase()) || row.getTitle().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    messageFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = messageFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                messageFiltered = (ArrayList<Message>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle, itemTime;
        private AppCompatImageView itemImage;
        CustomViewHolder(View itemView) {
            super(itemView);

            itemTitle = itemView.findViewById(R.id.item_title);
            itemImage = itemView.findViewById(R.id.featured_img);
            itemTime = itemView.findViewById(R.id.item_desc);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // send selected drug in callback
//                    try {
//                        listener.onMessageSelected(messageFiltered.get(getAdapterPosition()));
//                    }catch (Exception ignored){}
//                }
//            });
        }
    }

    private void loadImageFromDB(final Message message, final AppCompatImageView itemImage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] bytes = message.getImagestring();
                    // Show Image from DB in ImageView
                    itemImage.post(new Runnable() {
                        @Override
                        public void run() {
                            itemImage.setImageBitmap(AppUtils.getImage(bytes));

                        }
                    });
                } catch (Exception e) {
                    Log.e("Tag", "<loadImageFromDB> Error : " + e.getLocalizedMessage());
                }
            }
        }).start();
    }



}
