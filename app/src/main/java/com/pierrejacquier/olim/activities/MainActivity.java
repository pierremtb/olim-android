package com.pierrejacquier.olim.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.athingunique.ddbs.DriveSyncController;
import com.github.athingunique.ddbs.NewerDatabaseCallback;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.DbHelper;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.data.User;
import com.pierrejacquier.olim.fragments.LoadingFragment;
import com.pierrejacquier.olim.fragments.TagsFragment;
import com.pierrejacquier.olim.fragments.TasksFragment;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity
        extends AppCompatActivity
        implements NewerDatabaseCallback,
            NavigationView.OnNavigationItemSelectedListener,
            TasksFragment.OnFragmentInteractionListener,
            TagsFragment.OnFragmentInteractionListener {

    Olim app;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1;
    public static MaterialDialog loadingDialog;
    public static ActionBar actionBar;
    private static final int REQUEST_LOGIN = 0;
    private String currentFragmentName = null;
    private MenuItem filterMenu;
    private Menu actionsMenu;
    private final int FILTER_MENU_POSITION = 1;
    private DbHelper dbHelper;
    private DriveSyncController syncController;

    TextView drawerFullName;
    TextView drawerEmail;
    NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (Olim) getApplicationContext();

        // Initiate SQLiteOpenHelper
        dbHelper = new DbHelper(this);

        // Initiate DriveSyncController
        syncController = DriveSyncController.get(this, dbHelper, this).setDebug(true);

        // Set data
        List<Task> tasks = dbHelper.getTasksFromDatabase();
        List<Tag> tags = dbHelper.getTagsFromDatabase();
        String fullName = "User Name";
        String email = "user@name.do";
        app.setCurrentUser(new User(fullName, email, tasks, tags));

        // Do some layout stuff
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
        View header = navigationView.getHeaderView(0);
        drawerFullName = (TextView) header.findViewById(R.id.drawerFullName);
        drawerEmail = (TextView) header.findViewById(R.id.drawerEmail);
        drawerFullName.setText(fullName);
        drawerEmail.setText(email);
        prepareMenus();
        showTasksFragment();
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
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    public void driveNewer() {
        syncController.pullDbFromDrive();
        toaster("Cloud newer");
        getTasksFragment().endRefreshing();
    }

    @Override
    public void localNewer() {
        syncController.putDbInDrive();
        toaster("Local newer");
        getTasksFragment().endRefreshing();
    }

    /**
     * Navigation handling methods
     */

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

    private void launchSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Display handling methods
     */

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

    private void prepareMenus() {
    }

    private static void dismissLoadingDialog() {
        loadingDialog.dismiss();
    }

    public void toast(String str) {
        Context context = getApplicationContext();
        CharSequence text = str;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void toaster(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Data handling methods
     */

    public void insertTask(Task task) {
        dbHelper.putTaskInDatabase(task);
        showTasksFragment();
        updateUserTasks();
    }

    public void setThisTaskStatus(long id, long status) {
        dbHelper.setThisTaskStatus(id, status);
        showTasksFragment();
        updateUserTasks();
    }

    public void updateUserTasks() {
        app.getCurrentUser().setTasks(dbHelper.getTasksFromDatabase());
    }

    public void refreshData() {
        syncController.isDriveDbNewer();
    }

    //TODO: find a way to get account info
    private String getAccountName() {
        String possibleEmail = null;
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
            }
        }
        return possibleEmail;
    }
}
