package com.pierrejacquier.olim.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.github.athingunique.ddbs.DriveSyncController;
import com.github.athingunique.ddbs.NewerDatabaseCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
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
import com.pierrejacquier.olim.databinding.ActivityMainBinding;
import com.pierrejacquier.olim.fragments.LoadingFragment;
import com.pierrejacquier.olim.fragments.TagsFragment;
import com.pierrejacquier.olim.fragments.TasksFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements NewerDatabaseCallback,
            TasksFragment.OnFragmentInteractionListener,
            TagsFragment.OnFragmentInteractionListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Olim app;
    private ActionBar actionBar;
    private String currentFragmentName = null;
    private Menu actionsMenu;
    private DriveSyncController syncController;
    private Toolbar toolbar;
    private Drawer drawer = null;
    private Context context;
    private ActivityMainBinding binding;

    private final static int DRAWER_TASKS = 1;
    private final static int DRAWER_TAGS = 2;
    private final static int DRAWER_DIVIDER = 3;
    private final static int DRAWER_SETTINGS = 4;
    private final static int DRAWER_ABOUT = 5;
    private final static int DRAWER_SIGNOUT = 6;

    private final static int INTRO_ACTIVITY = 2;
    private final static int SIGNIN_RESOLUTION = 3;

    private final static int PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();
        context = this;
        app.setDatabase(context);

        app.setGoogleApiClient(new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        );

        List<Task> tasks = app.getDatabase().getTasks();
        List<Tag> tags = app.getDatabase().getTags();
        app.setCurrentUser(new User("User Name", "user@name.do", tasks, tags));

        // Do some layout stuff
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Tasks");
        }
        showLoadingFragment();
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
                List<Task> tasks = app.getDatabase().getTasks(getTasksFragment().getCurrentTag());
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
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    public void driveNewer() {
        Log.d("auie","Cloud newer");
        app.getGoogleSync().pullDbFromDrive();
        getTasksFragment().endRefreshing();
    }

    @Override
    public void localNewer() {
        Log.d("auie","Cloud newer");
        toast("Local newer");
        app.getGoogleSync().putDbInDrive();
        getTasksFragment().endRefreshing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTRO_ACTIVITY:
            case SIGNIN_RESOLUTION:
                Log.d("auie", "resooo");
                app.setGoogleApiClient(new GoogleApiClient.Builder(this)
                        .addApi(Drive.API)
                        .addApi(Plus.API)
                        .addScope(Plus.SCOPE_PLUS_PROFILE)
                        .addScope(Drive.SCOPE_APPFOLDER)
                        .addScope(Drive.SCOPE_FILE)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build()
                );

                app.getGoogleApiClient().connect();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    app.setReadContactsAllowed(true);
                }
                else {
                    app.setReadContactsAllowed(false);
                }

                buildDrawer();
                showTasksFragment();
            }
        }
    }

    /**
     * Navigation handling methods
     */

    private void showTasksFragment() {
        Fragment TasksFG = new TasksFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, TasksFG, "TasksFragment");
        actionBar.setTitle("Tasks");
        currentFragmentName = "TasksFragment";
        ft.commit();
    }

    private void showTagsFragment() {
//      TODO: make Filter and Search actions disappear on TagsFragment
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

    private void signOut() {
        app.wipeData();
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

    private void showSnack(String text) {
        Snackbar.make(binding.mainLayout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

                showSnack("Reading contacts enables a nice drawer header with your info");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        PERMISSIONS_REQUEST_GET_ACCOUNTS);
        } else {
            buildDrawer();
            showTasksFragment();
        }
    }

    private void setupMainActivity() {
        requestPermissions();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Data handling methods
     */

    private void fetchGoogle() {
        Plus.PeopleApi.loadVisible(app.getGoogleApiClient(), null);
        Person person = Plus.PeopleApi.getCurrentPerson(app.getGoogleApiClient());
        if (person != null) {
            String fullName = person.getDisplayName();
            String email = Plus.AccountApi.getAccountName(app.getGoogleApiClient());
            app.setCurrentUser(new User(fullName, email, null, null));
            app.getCurrentUser().setCoverUrl(person.getCover().getCoverPhoto().getUrl());
            app.getCurrentUser().setPictureUrl(person.getImage().getUrl());
        }
    }

    public void insertTask(Task task) {
        app.getDatabase().insertTask(task);
        showTasksFragment();
    }

    public void updateTask(Task task) {
        app.getDatabase().updateTask(task);
        showTasksFragment();
    }

    public Tag getTag(long id) {
        return app.getDatabase().getTag(id);
    }

    public List<Task> getTasks(Tag tag) {
        return app.getDatabase().getTasks(tag);
    }

    public List<Tag> getTags() {
        return app.getDatabase().getTags();
    }

    public List<Task> updateUserTasks() {
        List<Task> tasks = app.getDatabase().getTasks();
        app.getCurrentUser().setTasks(app.getDatabase().getTasks());
        return tasks;
    }

    public List<Tag> updateUserTags() {
        List<Tag> tags = app.getDatabase().getTags();
        app.getCurrentUser().setTags(app.getDatabase().getTags());
        return tags;
    }

    public void refreshData() {
        app.getGoogleSync().isDriveDbNewer();
        getTasksFragment().endRefreshing();
    }

    private void buildDrawer() {
        if (app == null) {
            return;
        }

        AccountHeader headerResult = null;

        if (app.isReadContactsAllowed()) {
            fetchGoogle();
            headerResult = new AccountHeaderBuilder()
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
        } else {
            headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.background)
                    .build();
        }

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
                                .withSelectable(false),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_SIGNOUT)
                                .withName(R.string.navigation_drawer_signout)
                                .withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
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
                            case DRAWER_SIGNOUT:
                                signOut();
                                break;
                            default: break;
                        }
                        return false;
                    }
                })
                .build();
        if (app.isReadContactsAllowed()) {
            ImageView coverView = headerResult.getHeaderBackgroundView();
            Glide.with(this).load(app.getCurrentUser().getCoverUrl()).into(coverView);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        syncController = DriveSyncController.get(context, app.getDatabase(), this);
        app.setGoogleSync(syncController);
        setupMainActivity();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, SIGNIN_RESOLUTION);
                app.getGoogleApiClient().connect();
            } catch (IntentSender.SendIntentException e) {
                app.getGoogleApiClient().connect();
            }
        }
    }
}
