package com.pierrejacquier.olim.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.pierrejacquier.olim.helpers.DriveApiFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pierrejacquier.olim.helpers.DriveSyncController;
import com.github.athingunique.ddbs.NewerDatabaseCallback;
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
import com.pierrejacquier.olim.databinding.NavHeaderMainBinding;
import com.pierrejacquier.olim.fragments.LoadingFragment;
import com.pierrejacquier.olim.fragments.TagsFragment;
import com.pierrejacquier.olim.fragments.TasksFragment;
import com.pierrejacquier.olim.helpers.DbHelper;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity
        extends AppCompatActivity
        implements NewerDatabaseCallback,
            NavigationView.OnNavigationItemSelectedListener,
            TasksFragment.OnFragmentInteractionListener,
            TagsFragment.OnFragmentInteractionListener,
        DriveSyncController.GoogleApiClientCallbacks
        {

    private Olim app;
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
    private GoogleApiClient googleApiClient;
    private Bitmap profilePicture;
    private AccountHeader headerResult;
    private Drawer drawer;
    private Toolbar toolbar;
    
    final static int DRAWER_TASKS = 0;
    final static int DRAWER_TAGS = 1;
    final static int DRAWER_SETTINGS = 2;
    final static int DRAWER_SIGNOUT = 3;

    TextView drawerFullName;
    TextView drawerEmail;
    NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (Olim) getApplicationContext();
        String fullName = "User Name";
        String email = "user@name.do";
        app.setCurrentUser(new User(fullName, email, null, null));
        super.onCreate(savedInstanceState);
        // Initiate SQLiteOpenHelper
        dbHelper = new DbHelper(this);
        List<Task> tasks = dbHelper.getTasks();
        List<Tag> tags = dbHelper.getTags();
        fullName = "User Name";
        email = "user@name.do";
        app.setCurrentUser(new User(fullName, email, tasks, tags));

        Log.d("MainActivity@96", "ONCREATE");
        // Initiate DriveSyncController
        syncController = DriveSyncController.get(this, dbHelper, this).setDebug(true);

        setContentView(R.layout.activity_main);



        // Set data
//        dbHelper.clearDatabase();
//        Tag tag = new Tag().withName("First").withComments("Yeah").withColor("#000000").withIcon("add");
//        dbHelper.insertTag(tag);

        // Do some layout stuff
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setTitle("Tasks");
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.background)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(app.getCurrentUser().getFullName())
                                .withEmail("work@pierrejacquier.com")
                                .withIcon(GoogleMaterial.Icon.gmd_account_circle)
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
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_SETTINGS)
                                .withName(R.string.navigation_drawer_settings)
                                .withIcon(GoogleMaterial.Icon.gmd_settings)
                        ,
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_SIGNOUT)
                                .withName(R.string.navigation_drawer_signout)
                                .withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
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
                            default: break;
                        }
                        return false;
                    }
                })
                .build();
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//        NavHeaderMainBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.nav_header_main, navigationView, false);
//        navigationView.addHeaderView(binding.getRoot());
//        binding.setUser(app.getCurrentUser());
        prepareMenus();
        showTasksFragment();
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
            super.onBackPressed();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        actionsMenu = menu;

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
                List<Task> filteredTasks = new ArrayList<Task>();
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
                getTasksFragment().reRenderTasks();
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

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
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
        dbHelper.insertTask(task);
        showTasksFragment();
        updateUserTasks();
    }

    public void updateTask(Task task) {
        dbHelper.updateTask(task);
        showTasksFragment();
        updateUserTasks();
    }

    public Tag getTag(long id) {
        return dbHelper.getTag(id);
    }

    public List<Task> getTasks(Tag tag) {
        return dbHelper.getTasks(tag);
    }

    public void setThisTaskStatus(long id, long status) {
        dbHelper.setThisTaskStatus(id, status);
        showTasksFragment();
        updateUserTasks();
    }

    public void updateUserTasks() {
        app.getCurrentUser().setTasks(dbHelper.getTasks());
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

    @Override
    public void onGoogleConnected() {
        Log.d("MainActivity@464", "GGGGGGGGGGGGGGGGGGGGGGGGGG " );
        //Log.d("MainActivity@488", app.toString());
    }
}
