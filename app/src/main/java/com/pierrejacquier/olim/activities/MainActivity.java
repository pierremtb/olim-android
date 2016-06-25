package com.pierrejacquier.olim.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.data.User;
import com.pierrejacquier.olim.fragments.LoadingFragment;
import com.pierrejacquier.olim.fragments.TagsFragment;
import com.pierrejacquier.olim.fragments.TasksFragment;
import com.pierrejacquier.olim.helpers.DbHelper;
import com.pierrejacquier.olim.helpers.DriveSyncController;
import com.pierrejacquier.olim.helpers.NewerDatabaseCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements NewerDatabaseCallback,
            TasksFragment.OnFragmentInteractionListener,
            TagsFragment.OnFragmentInteractionListener,
            DriveSyncController.GoogleApiClientCallbacks {

    private Olim app;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    private ActionBar actionBar;
    private String currentFragmentName = null;
    private Menu actionsMenu;
    private DbHelper dbHelper;
    private DriveSyncController syncController;
    private Toolbar toolbar;
    private Drawer drawer = null;

    private final static int DRAWER_TASKS = 1;
    private final static int DRAWER_TAGS = 2;
    private final static int DRAWER_DIVIDER = 3;
    private final static int DRAWER_SETTINGS = 4;
    private final static int DRAWER_ABOUT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();

        String fullName = "User Name";
        String email = "user@name.do";
        dbHelper = new DbHelper(this);
        List<Task> tasks = dbHelper.getTasks();
        List<Tag> tags = dbHelper.getTags();
        app.setCurrentUser(new User(fullName, email, tasks, tags));

        // Do some layout stuff
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Tasks");
            // TODO: set an elevation when scrolling
        }
        requestPermissions();
        showTasksFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        actionsMenu = menu;
        // TODO: fix the missing Close action
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        theTextArea.setTextColor(getResources().getColor(R.color.colorPrimaryText));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean opening) {
                if (opening) {
                    getTasksFragment().hideTaskAdder();
                } else {
                    getTasksFragment().showTaskAdder();
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Task> tasks = dbHelper.getTasks(getTasksFragment().getCurrentTag());
                List<Task> filteredTasks = new ArrayList<>();
                if (newText.equals("")) {
                    filteredTasks = tasks;
                } else {
                    for (Task task : tasks) {
                        if (task.getTitle() != null &&
                            task.getTitle().toUpperCase().contains(newText.toUpperCase())) {
                            filteredTasks.add(task);
                        }
                    }
                }
                app.getCurrentUser().setTasks(filteredTasks);
                getTasksFragment().reRenderTasksTemp();
                return false;
            }
        });
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
                    getTasksFragment().hideTaskAdder();
                }
                break;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    public void driveNewer() {
        syncController.pullDbFromDrive();
        toast("Cloud newer");
        getTasksFragment().endRefreshing();
    }

    @Override
    public void localNewer() {
        syncController.putDbInDrive();
        toast("Local newer");
        getTasksFragment().endRefreshing();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSync(true);
                } else {
                    startSync(false);
                }
            }
        }
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
//        if (actionsMenu != null) {
//            MenuItem item = actionsMenu.getItem(0);
//            item.setIcon(getResources().getDrawable(R.drawable.ic_add));
//            if (item != null) {
//                // TODO: make Filter and Search actions disappear on TagsFragment
//                item.setVisible(false);
//                ActivityCompat.invalidateOptionsMenu(this);
//            }
//        }
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

    private void launchAbout() {
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityTitle(getResources().getString(R.string.navigation_drawer_about))
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutDescription(getResources().getString(R.string.app_description))
                .start(this);
    }

    /**
     * Display handling methods
     */

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // TODO: Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            }
        } else {
            startSync(true);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Data handling methods
     */

    private void startSync(boolean googleAuthorized) {
        syncController = DriveSyncController.get(this, dbHelper, this, app, googleAuthorized).setDebug(true);
    }

    public void insertTask(Task task) {
        dbHelper.insertTask(task);
        showTasksFragment();
    }

    public void updateTask(Task task) {
        dbHelper.updateTask(task);
        showTasksFragment();
    }

    public void syncDb() {
        syncController.isDriveDbNewer();
        updateUserTasks();
        updateUserTags();
    }

    public Tag getTag(long id) {
        return dbHelper.getTag(id);
    }

    public List<Task> getTasks(Tag tag) {
        return dbHelper.getTasks(tag);
    }

    public List<Tag> getTags() {
        return dbHelper.getTags();
    }

    public List<Task> updateUserTasks() {
        List<Task> tasks = dbHelper.getTasks();
        app.getCurrentUser().setTasks(dbHelper.getTasks());
        return tasks;
    }

    public List<Tag> updateUserTags() {
        List<Tag> tags = dbHelper.getTags();
        app.getCurrentUser().setTags(dbHelper.getTags());
        return tags;
    }

    public void refreshData() {
        syncController.isDriveDbNewer();
    }

    @Override
    public void onGoogleConnected(Olim app) {
        if (app == null) {
            return;
        }

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.background)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(app.getCurrentUser().getFullName())
                                .withEmail(app.getCurrentUser().getEmail())
                                .withIcon(app.getCurrentUser().getPictureUrl())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withIdentifier(DRAWER_TASKS)
                                .withName(R.string.navigation_drawer_tasks)
                                .withIcon(GoogleMaterial.Icon.gmd_done_all),
                        new PrimaryDrawerItem()
                                .withIdentifier(DRAWER_TAGS)
                                .withName(R.string.navigation_drawer_tags)
                                .withIcon(GoogleMaterial.Icon.gmd_label_outline),
                        new DividerDrawerItem().withIdentifier(DRAWER_DIVIDER),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_SETTINGS)
                                .withName(R.string.navigation_drawer_settings)
                                .withIcon(GoogleMaterial.Icon.gmd_settings)
                                .withSelectable(false),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_ABOUT)
                                .withName(R.string.navigation_drawer_about)
                                .withIcon(GoogleMaterial.Icon.gmd_info_outline)
                                .withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case DRAWER_TASKS:
                                showTasksFragment();
                                break;
                            case DRAWER_TAGS:
                                showTagsFragment();
                                break;
                            case DRAWER_SETTINGS:
                                launchSettings();
                                break;
                            case DRAWER_ABOUT:
                                launchAbout();
                                break;
                            default: break;
                        }
                        return false;
                    }
                })
                .build();
        ImageView coverView = headerResult.getHeaderBackgroundView();
        Glide.with(this).load(app.getCurrentUser().getCoverUrl()).into(coverView);
    }
}
