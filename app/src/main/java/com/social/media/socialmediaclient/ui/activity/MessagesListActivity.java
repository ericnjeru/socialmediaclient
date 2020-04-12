package com.social.media.socialmediaclient.ui.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.social.media.socialmediaclient.AppConstants;
import com.social.media.socialmediaclient.R;
import com.social.media.socialmediaclient.model.Message;
import com.social.media.socialmediaclient.repository.MessageRepository;
import com.social.media.socialmediaclient.ui.adapter.MessagesListAdapter;
import com.social.media.socialmediaclient.util.NavigatorUtils;
import com.social.media.socialmediaclient.util.RecyclerItemClickListener;

import java.util.List;
import java.util.Objects;

public class MessagesListActivity extends AppCompatActivity implements View.OnClickListener,
        RecyclerItemClickListener.OnRecyclerViewItemClickListener, AppConstants {


    private TextView emptyView;
    private RecyclerView recyclerView;
    private MessagesListAdapter messagesListAdapter;
    private SearchView searchView;
    private MessageRepository messageRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        // toolbar fancy stuff
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        messageRepository = new MessageRepository(getApplicationContext());

        recyclerView = findViewById(R.id.task_list);
        whiteNotificationBar(recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2 , StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        emptyView = findViewById(R.id.empty_view);

        updateTaskList();
    }

    private void updateTaskList() {
        messageRepository.getTasks().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                if(messages.size() > 0) {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (messagesListAdapter == null) {
                        messagesListAdapter = new MessagesListAdapter(this,messages);
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
     * New note to be added
     * */
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(MessagesListActivity.this, AddMessageActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }


    /*
     * update/delete existing note
     * */
    @Override
    public void onItemClick(View parentView, View childView, int position) {
        Message message = messagesListAdapter.getItem(position);
        if(message.isEncrypt()) {
            NavigatorUtils.redirectToPwdScreen(this, message);

        } else {
            NavigatorUtils.redirectToEditTaskScreen(this, message);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if(data.hasExtra(INTENT_TASK)) {
                if(data.hasExtra(INTENT_DELETE)) {
                    messageRepository.deleteTask((Message) data.getSerializableExtra(INTENT_TASK));

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

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                messagesListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                messagesListAdapter.getFilter().filter(query);
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


}
