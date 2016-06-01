package com.pierrejacquier.olim.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.SubscribeListener;
import im.delight.android.ddp.db.memory.InMemoryDatabase;

import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.data.User;
import com.pierrejacquier.olim.fragments.LoadingFragment;
import com.pierrejacquier.olim.fragments.TagsFragment;
import com.pierrejacquier.olim.fragments.TasksFragment;

public class MainActivity
        extends AppCompatActivity
        implements MeteorCallback,
            NavigationView.OnNavigationItemSelectedListener,
            TasksFragment.OnFragmentInteractionListener,
            TagsFragment.OnFragmentInteractionListener {

    Olim app;
    private Meteor meteor;
    private boolean subscribed = false;
    public static MaterialDialog loadingDialog;
    public static ActionBar actionBar;
    private static final int REQUEST_LOGIN = 0;
    private String currentFragmentName = null;
    private MenuItem filterMenu;
    private Menu actionsMenu;
    private final int FILTER_MENU_POSITION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (Olim) getApplicationContext();

        // Layout stuff
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setTitle("Tasks");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Start Meteor connection
        try {
            //meteor = MeteorSingleton.createInstance(this, "ws://olim.herokuapp.com/websocket", new InMemoryDatabase());
            meteor = MeteorSingleton.createInstance(this, "ws://192.168.0.103:3000/websocket", new InMemoryDatabase());
            meteor.addCallback(this);
            meteor.connect();

            /*loadingDialog = new MaterialDialog.Builder(this)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .show();*/
            showLoadingFragment();
        } catch (Exception e) {
            getTasksFragment().showSnack("Failed. Try again");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        actionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_filter:
                if (currentFragmentName.equals("TasksFragment")) {
                    TasksFragment tasksFragment = (TasksFragment) getSupportFragmentManager().findFragmentByTag("TasksFragment");
                    tasksFragment.showTagsFilteringDialog();
                }
                break;
            case R.id.action_search:
                if (currentFragmentName.equals("TasksFragment")) {
                    getTasksFragment().showSnack("Search is not available, yet");
                }
                break;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.navigation_drawer_tasks:
                showTasksFragment();
                break;
            case R.id.navigation_drawer_tags:
                showTagsFragment();
                break;
            case R.id.navigation_drawer_settings:
                launchSettings();
                break;
            case R.id.navigation_drawer_signout:
                if (app.getCurrentUser() != null) {
                    meteor.logout();
                }
                launchLogin();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Meteor methods

    @Override
    public void onConnect(boolean signedInAutomatically) {
        if (signedInAutomatically && meteor.isLoggedIn()) {
            app.setCurrentUser(new User(
                    meteor.getDatabase()
                            .getCollection("users")
                            .getDocument(meteor.getUserId())
                )
            );
            prepareBoard(false);
        } else {
            launchLogin();
        }
    }

    @Override
    public void onDisconnect() {}

    @Override
    public void onException(Exception e) {}

    @Override
    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {
        updateCurrentView(collectionName);
    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        updateCurrentView(collectionName);
        if (collectionName.equals("Tasks")) {
            Task task = new Task(meteor.getDatabase().getCollection("Tasks").getDocument(documentID));
            Log.d("MainActivity@198", task.toString());
            if (currentFragmentName.equals("TasksFragment")) {
            }
        }
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        updateCurrentView(collectionName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            if (requestCode == REQUEST_LOGIN) {
                prepareBoard(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        MeteorSingleton.getInstance().disconnect();
        MeteorSingleton.getInstance().removeCallback(this);

        super.onDestroy();
    }

    // Navigation

    public void showTasksFragment() {
        Fragment TasksFG = new TasksFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, TasksFG, "TasksFragment");
        actionBar.setTitle("Tasks");
        currentFragmentName = "TasksFragment";
        ft.commit();
    }

    private void showTagsFragment() {
        if (actionsMenu != null) {
            MenuItem item = actionsMenu.getItem(0);
            item.setIcon(getResources().getDrawable(R.drawable.ic_add));
            if (item != null) {
                // TODO: make it disappear for real -_-
                item.setVisible(false);
                ActivityCompat.invalidateOptionsMenu(this);
            }
        }
        Fragment TagsFG = new TagsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, TagsFG);
        actionBar.setTitle("Tags");
        currentFragmentName = "TagsFragment";
        ft.commit();
    }

    private void showLoadingFragment() {
        Fragment loadingFragment = new LoadingFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, loadingFragment);
        actionBar.setTitle("Olim");
        currentFragmentName = "LoadingFragment";
        ft.commit();
    }

    private TasksFragment getTasksFragment() {
        return (TasksFragment) getSupportFragmentManager().findFragmentByTag("TasksFragment");
    }

    private void launchLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void launchSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    // Display

    private void updateCurrentView(String collectionName) {
        if (currentFragmentName == null) {
            return;
        }

        switch (currentFragmentName) {
            case "TasksFragment":
                if (collectionName.equals("Tasks")) {
                    TasksFragment tasksFragment = (TasksFragment) getSupportFragmentManager()
                                                    .findFragmentByTag("TasksFragment");
                    //tasksFragment.reRenderTasks();
                    showTasksFragment();
                }
                break;
            case "TagsFragment":
                if (collectionName.equals("Tags")) {
                    showTagsFragment();
                }
                break;
        }

    }

    public void toast(String str) {
        Context context = getApplicationContext();
        CharSequence text = str;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void prepareBoard(final boolean doGet) {
        if(true) {
            meteor.subscribe("all-user-data-tasks-tags", new Object[]{}, new SubscribeListener() {
                @Override
                public void onSuccess() {
                    subscribed = true;
                    prepareActionBar();
                    showTasksFragment();
                }

                @Override
                public void onError(String error, String reason, String details) {
                    toast(error + "-" + reason);
                    System.out.println(details);
                }
            });
        } else {
            prepareActionBar();
            showTagsFragment();
        }
    }

    private void prepareActionBar() {
        TextView drawerFullName = (TextView) findViewById(R.id.drawerFullName);
        TextView drawerEmail = (TextView) findViewById(R.id.drawerEmail);
        drawerFullName.setText(app.getCurrentUser().getFullName());
        drawerEmail.setText(app.getCurrentUser().getEmail());
    }

    private static void dismissLoadingDialog() {
        loadingDialog.dismiss();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }
}
