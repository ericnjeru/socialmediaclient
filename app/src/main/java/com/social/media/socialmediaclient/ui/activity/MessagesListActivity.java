package com.social.media.socialmediaclient.ui.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.social.media.socialmediaclient.AppConstants;
import com.social.media.socialmediaclient.R;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.repository.MessageRepository;
import com.social.media.socialmediaclient.ui.adapter.MessagesListAdapter;
import com.social.media.socialmediaclient.util.AlertDialogHelper;
import com.social.media.socialmediaclient.util.NavigatorUtils;
import com.social.media.socialmediaclient.util.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessagesListActivity extends AppCompatActivity implements View.OnClickListener,
        RecyclerTouchListener.ClickListener, AppConstants, AlertDialogHelper.AlertDialogListener{


    private TextView emptyView;
    private RecyclerView recyclerView;
    private MessagesListAdapter messagesListAdapter;
    private SearchView searchView;
    private MessageRepository messageRepository;
    ActionMode mActionMode;
    Menu context_menu;
    boolean isMultiSelect = false;
    ArrayList<Message> mesages = new ArrayList<>();
    ArrayList<Message> multiselect_list = new ArrayList<>();
    AlertDialogHelper alertDialogHelper;
    private Message data_to_delete = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar fancy stuff
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        alertDialogHelper =new AlertDialogHelper(this);

        messageRepository = new MessageRepository(getApplicationContext());

        recyclerView = findViewById(R.id.task_list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2 , StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, this));

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        emptyView = findViewById(R.id.empty_view);

        updateTaskList();
    }

    private void updateTaskList() {
        messageRepository.getTasks().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                assert messages != null;
                if(messages.size() > 0) {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (messagesListAdapter == null) {
                        messagesListAdapter = new MessagesListAdapter(this,messages,multiselect_list);
                        recyclerView.setAdapter(messagesListAdapter);

                    } else messagesListAdapter.addTasks(messages);
                } else updateEmptyView();
            }
        });
    }

    private void updateEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }


    /*
     * New message to be added
     * */
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(MessagesListActivity.this, AddMessageActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }


    /*
     * update/delete existing meessage
     * */
    @Override
    public void onClick(View view, final int position)  {
        if (isMultiSelect) {
            multi_select(position);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View layout = LayoutInflater.from(this).inflate(R.layout.dialog_item_action, null);
            builder.setView(layout);

            final com.an.customfontview.CustomTextView view_Action = layout.findViewById(R.id.view_action);
            final com.an.customfontview.CustomTextView edit_Action = layout.findViewById(R.id.edit_action);

                //        builder.setPositiveButton("ok ", null);
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
                    Message message = messagesListAdapter.getItem(position);
                    if(message.isEncrypt()) {
                        NavigatorUtils.redirectToPwdScreen(MessagesListActivity.this, message);
                        dialog.dismiss();
                    } else {
                        NavigatorUtils.redirectToEditMessageScreen(MessagesListActivity.this, message);
                        dialog.dismiss();
                    }
                }
            });

            view_Action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = messagesListAdapter.getItem(position);
                    NavigatorUtils.redirectToMessaeDetailScreen(MessagesListActivity.this, message);
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onLongClick(View view,final int position){
        if (!isMultiSelect) {
            multiselect_list = new ArrayList<>();
            isMultiSelect = true;

            if (mActionMode == null) {
                mActionMode = startActionMode(mActionModeCallback);
            }
        }

        multi_select(position);
    }
    // Add/Remove the item from/to the list

    public void multi_select(int position) {
        Message message = messagesListAdapter.getItem(position);
        if (mActionMode != null) {
            if (multiselect_list.contains(message))
                multiselect_list.remove(message);
            else
                multiselect_list.add(message);

            if (multiselect_list.size() > 0)
                mActionMode.setTitle("" + multiselect_list.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();
        }
    }
        public void refreshAdapter()
        {
            messageRepository.getTasks().observe(this, new Observer<List<Message>>() {
                @Override
                public void onChanged(List<Message> messages) {
                    messagesListAdapter.messageSelected=multiselect_list;
                    messagesListAdapter.messages=mesages;
                    messagesListAdapter.notifyDataSetChanged();
                }
            });
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if(data.hasExtra(INTENT_TASK)) {
                if(data.hasExtra(INTENT_DELETE)) {
                    data_to_delete = (Message) data.getSerializableExtra(INTENT_TASK);
                    alertDialogHelper.showAlertDialog("", "Delete Message", "DELETE", "CANCEL", 3, false);

                } else {
                    messageRepository.updateTask((Message) data.getSerializableExtra(INTENT_TASK));
                }
            } else {
                String title = data.getStringExtra(INTENT_TITLE);
                String desc = data.getStringExtra(INTENT_DESC);
                byte[] imgbyte = data.getByteArrayExtra(INTENT_IMGBYT);
                String pwd = data.getStringExtra(INTENT_PWD);
                boolean encrypt = data.getBooleanExtra(INTENT_ENCRYPT, false);
                messageRepository.insertTask(title, desc, imgbyte, encrypt, pwd);
            }
            updateTaskList();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searching_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MaterialSearchView searchView = findViewById(R.id.search_view);
        searchView.setMenuItem(item);

        // listening to search query text change
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                messagesListAdapter.getFilter().filter(query);
                return false;
        //                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // filter recycler view when text is changed
                messagesListAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            getMenuInflater().inflate(R.menu.contextual_action, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                alertDialogHelper.showAlertDialog("", "Delete Message", "DELETE", "CANCEL", 1, false);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list = new ArrayList<Message>();
            refreshAdapter();
        }
    };

    // AlertDialog Callback Functions

    @Override
    public void onPositiveClick(int from) {
        if(from==1)
        {
            messageRepository.getTasks().observe(this, new Observer<List<Message>>() {
                @Override
                public void onChanged(List<Message> messages) {
                    if(multiselect_list.size()>0)
                    {
                        for(int i=0;i<multiselect_list.size();i++)
//                    mesages.remove(multiselect_list.get(i));
                            messageRepository.deleteTask(multiselect_list.get(i));


                        messagesListAdapter.notifyDataSetChanged();

                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                        Toast.makeText(getApplicationContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else if(from==2)
        {
            messageRepository.getTasks().observe(this, new Observer<List<Message>>() {
                @Override
                public void onChanged(List<Message> messages) {
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }

                    Message message = new Message();
                    mesages.add(message);
                    messagesListAdapter.notifyDataSetChanged();
                }
            });
        }else if (from==3){
            messageRepository.deleteTask(data_to_delete);
        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }
}
