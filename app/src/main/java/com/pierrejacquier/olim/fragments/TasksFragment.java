package com.pierrejacquier.olim.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.NinePatchDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.activities.MainActivity;
import com.pierrejacquier.olim.adapters.SwipeableTaskAdapter;
import com.pierrejacquier.olim.adapters.TagsListAdapter;
import com.pierrejacquier.olim.data.User;
import com.pierrejacquier.olim.helpers.CustomLinearLayoutManager;
import com.pierrejacquier.olim.helpers.Graphics;
import com.pierrejacquier.olim.helpers.TaskEventsListener;
import com.pierrejacquier.olim.helpers.Tools;
import com.rengwuxian.materialedittext.MaterialEditText;

import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TasksFragment
        extends Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    Olim app;
    private OnFragmentInteractionListener Main;
    private MainActivity mainActivity;

    private ArrayList<Task> overdueTasks = new ArrayList<>();
    private ArrayList<Task> todayTasks = new ArrayList<>();
    private ArrayList<Task> tomorrowTasks = new ArrayList<>();
    private ArrayList<Task> inTheNextSevenDaysTasks = new ArrayList<>();
    private ArrayList<Task> laterTasks = new ArrayList<>();

    private List<Tag> tags = new ArrayList<>();
    private Tag currentTag = null;

    private CoordinatorLayout tasksCoordinatorLayout;
    public View tasksView;

    private TextView currentTagChipLabel;
    private ImageButton currentTagChipIcon;
    private ImageButton currentTagChipIconDelete;
    private LinearLayout currentTagChipLayout;

    private ItemTouchHelper mItemTouchHelper;
    private ImageView newTaskClearButton;
    private LinearLayout previewTaskLayout;
    private TextView taskPrimaryText;
    private TextView taskSecondaryText;
    private MaterialEditText taskAdderInput;
    private ImageView taskAdderSendButton;

    private LinearLayout overdueTasksLayout;
    private LinearLayout todayTasksLayout;
    private LinearLayout tomorrowTasksLayout;
    private LinearLayout inTheNextSevenDaysTasksLayout;
    private LinearLayout laterTasksLayout;
    private LinearLayout noTasksLayout;

    private RecyclerView overdueTasksRecyclerView;
    private RecyclerView todayTasksRecyclerView;
    private RecyclerView tomorrowTasksRecyclerView;
    private RecyclerView inTheNextSevenDaysTasksRecyclerView;
    private RecyclerView laterTasksRecyclerView;


    @InjectView(R.id.markAsDoneAllOverdueTasksImageView) ImageView markAsDoneAllOverdueTasksImageView;
    @InjectView(R.id.markAsDoneAllTodayTasksImageView) ImageView markAsDoneAllTodayTasksImageView;
    @InjectView(R.id.markAsDoneAllTomorrowTasksImageView) ImageView markAsDoneAllTomorrowTasksImageView;
    @InjectView(R.id.markAsDoneAllInTheNextSevenDaysTasksImageView) ImageView markAsDoneAllInTheNextSevenDaysTasksImageView;
    @InjectView(R.id.markAsDoneAllLaterTasksImageView) ImageView markAsDoneAllLaterTasksImageView;

    @InjectView(R.id.postponeAllOverdueTasksImageView) ImageView postponeAllOverdueTasksImageView;
    @InjectView(R.id.postponeAllTodayTasksImageView) ImageView postponeAllTodayTasksImageView;
    @InjectView(R.id.postponeAllTomorrowTasksImageView) ImageView postponeAllTomorrowTasksImageView;
    @InjectView(R.id.postponeAllInTheNextSevenDaysTasksImageView) ImageView postponeAllInTheNextSevenDaysTasksImageView;
    @InjectView(R.id.postponeAllLaterTasksImageView) ImageView postponeAllLaterTasksImageView;

    private CustomLinearLayoutManager overdueTasksLayoutManager;
    private RecyclerViewSwipeManager overdueTasksRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager overdueTasksRecyclerViewTouchActionGuardManager;
    private RecyclerView.Adapter overdueTasksWrappedAdapter;
    private SwipeableTaskAdapter overdueTasksItemAdapter;
    private GeneralItemAnimator overdueTasksAnimator;

    private CustomLinearLayoutManager todayTasksLayoutManager;
    private RecyclerViewSwipeManager todayTasksRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager todayTasksRecyclerViewTouchActionGuardManager;
    private RecyclerView.Adapter todayTasksWrappedAdapter;
    private SwipeableTaskAdapter todayTasksItemAdapter;
    private GeneralItemAnimator todayTasksAnimator;

    private CustomLinearLayoutManager tomorrowTasksLayoutManager;
    private RecyclerViewSwipeManager tomorrowTasksRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager tomorrowTasksRecyclerViewTouchActionGuardManager;
    private RecyclerView.Adapter tomorrowTasksWrappedAdapter;
    private SwipeableTaskAdapter tomorrowTasksItemAdapter;
    private GeneralItemAnimator tomorrowTasksAnimator;

    private CustomLinearLayoutManager inTheNextSevenDaysTasksLayoutManager;
    private RecyclerViewSwipeManager inTheNextSevenDaysTasksRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager;
    private RecyclerView.Adapter inTheNextSevenDaysTasksWrappedAdapter;
    private SwipeableTaskAdapter inTheNextSevenDaysTasksItemAdapter;
    private GeneralItemAnimator inTheNextSevenDaysTasksAnimator;

    private CustomLinearLayoutManager laterTasksLayoutManager;
    private RecyclerViewSwipeManager laterTasksRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager laterTasksRecyclerViewTouchActionGuardManager;
    private RecyclerView.Adapter laterTasksWrappedAdapter;
    private SwipeableTaskAdapter laterTasksItemAdapter;
    private GeneralItemAnimator laterTasksAnimator;
    

    public MaterialDialog tagsFilteringDialog;

    private Task newTask = new Task();

    public TasksFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getActivity().getApplicationContext();
        /*if (MeteorSingleton.getInstance().isLoggedIn()) {
            Document userDoc = MeteorSingleton.getInstance().getDatabase()
                                .getCollection("users")
                                .getDocument(MeteorSingleton.getInstance().getUserId());
            if (userDoc != null) {
                app.setCurrentUser(new User(userDoc));
            }
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_tasks, container, false);
        ButterKnife.inject(this, myView);

        tasksView = myView;
        tasksCoordinatorLayout = (CoordinatorLayout) myView.findViewById(R.id.tasksCoordinatorLayout);
        overdueTasksLayout = (LinearLayout) myView.findViewById(R.id.overdueTasksLayout);
        todayTasksLayout = (LinearLayout) myView.findViewById(R.id.todayTasksLayout);
        tomorrowTasksLayout = (LinearLayout) myView.findViewById(R.id.tomorrowTasksLayout);
        inTheNextSevenDaysTasksLayout = (LinearLayout) myView.findViewById(R.id.inTheNextSevenDaysTasksLayout);
        laterTasksLayout = (LinearLayout) myView.findViewById(R.id.laterTasksLayout);
        noTasksLayout = (LinearLayout) myView.findViewById(R.id.noTasksLayout);

        currentTagChipLabel = (TextView) myView.findViewById(R.id.currentTagChipLabel);
        currentTagChipIcon = (ImageButton) myView.findViewById(R.id.currentTagChipIcon);
        currentTagChipIconDelete = (ImageButton) myView.findViewById(R.id.currentTagChipDeleteIcon);
        currentTagChipLayout = (LinearLayout) myView.findViewById(R.id.currentTagChipLayout);

        overdueTasksLayout = (LinearLayout) myView.findViewById(R.id.overdueTasksLayout);
        todayTasksLayout = (LinearLayout) myView.findViewById(R.id.todayTasksLayout);
        tomorrowTasksLayout = (LinearLayout) myView.findViewById(R.id.tomorrowTasksLayout);
        inTheNextSevenDaysTasksLayout = (LinearLayout) myView.findViewById(R.id.inTheNextSevenDaysTasksLayout);
        laterTasksLayout = (LinearLayout) myView.findViewById(R.id.laterTasksLayout);
        noTasksLayout = (LinearLayout) myView.findViewById(R.id.noTasksLayout);

        postponeAllOverdueTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(overdueTasks);
            }
        });
        postponeAllTodayTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(todayTasks);
            }
        });
        postponeAllTomorrowTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(tomorrowTasks);
            }
        });
        postponeAllInTheNextSevenDaysTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(inTheNextSevenDaysTasks);
            }
        });
        postponeAllLaterTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(laterTasks);
            }
        });
        
        markAsDoneAllOverdueTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(overdueTasks);
            }
        });
        markAsDoneAllTodayTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(todayTasks);
            }
        });
        markAsDoneAllTomorrowTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(tomorrowTasks);
            }
        });
        markAsDoneAllInTheNextSevenDaysTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(inTheNextSevenDaysTasks);
            }
        });
        markAsDoneAllLaterTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(laterTasks);
            }
        });

        tags = app.getCurrentUser().getTags();
        fetchTasks(null);

        return myView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NestedScrollView tasksNestedScrollView = (NestedScrollView) view.findViewById(R.id.tasksNestedScrollView);
        tasksNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // TODO: Set appBar elevation if scrolled
            }
        });
        final List<Tag> tags = app.getCurrentUser().getTags();

        /*
        tagsFilteringDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.filter_with_tag)
                .autoDismiss(true)
                .positiveText(R.string.clear)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        clearCurrentTag();
                    }
                })
                .adapter(new TagsListAdapter(
                            getContext(),
                            tags.toArray(new Tag[tags.size()]), this),
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            }
                        })
                .build();

        ListView list = tagsFilteringDialog.getListView();
        list.setOnItemClickListener(this);*/

        // Overdue Tasks
        overdueTasksRecyclerView = (RecyclerView) view.findViewById(R.id.overdueTasksRecyclerView);
        overdueTasksLayoutManager = new CustomLinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);
        overdueTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        overdueTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        overdueTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        overdueTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        overdueTasksItemAdapter = new SwipeableTaskAdapter(overdueTasks);
        overdueTasksItemAdapter.setEventListener(new TaskEventsListener(overdueTasks));
        overdueTasksWrappedAdapter = overdueTasksRecyclerViewSwipeManager.createWrappedAdapter(overdueTasksItemAdapter);
        overdueTasksAnimator = new SwipeDismissItemAnimator();
        overdueTasksAnimator.setSupportsChangeAnimations(false);
        overdueTasksRecyclerView.setLayoutManager(overdueTasksLayoutManager);
        overdueTasksRecyclerView.setAdapter(overdueTasksWrappedAdapter);
        overdueTasksRecyclerView.setItemAnimator(overdueTasksAnimator);
        if (!supportsViewElevation()) {
            overdueTasksRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        overdueTasksRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        overdueTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(overdueTasksRecyclerView);
        overdueTasksRecyclerViewSwipeManager.attachRecyclerView(overdueTasksRecyclerView);

        // Today Tasks
        todayTasksRecyclerView = (RecyclerView) view.findViewById(R.id.todayTasksRecyclerView);
        todayTasksLayoutManager = new CustomLinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);
        todayTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        todayTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        todayTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        todayTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        todayTasksItemAdapter = new SwipeableTaskAdapter(todayTasks);
        todayTasksItemAdapter.setEventListener(new TaskEventsListener(todayTasks));
        todayTasksWrappedAdapter = todayTasksRecyclerViewSwipeManager.createWrappedAdapter(todayTasksItemAdapter);
        todayTasksAnimator = new SwipeDismissItemAnimator();
        todayTasksAnimator.setSupportsChangeAnimations(false);
        todayTasksRecyclerView.setLayoutManager(todayTasksLayoutManager);
        todayTasksRecyclerView.setAdapter(todayTasksWrappedAdapter);
        todayTasksRecyclerView.setItemAnimator(todayTasksAnimator);
        if (!supportsViewElevation()) {
            todayTasksRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        todayTasksRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        todayTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(todayTasksRecyclerView);
        todayTasksRecyclerViewSwipeManager.attachRecyclerView(todayTasksRecyclerView);

        // Tomorrow Tasks
        tomorrowTasksRecyclerView = (RecyclerView) view.findViewById(R.id.tomorrowTasksRecyclerView);
        tomorrowTasksLayoutManager = new CustomLinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);
        tomorrowTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        tomorrowTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        tomorrowTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        tomorrowTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        tomorrowTasksItemAdapter = new SwipeableTaskAdapter(tomorrowTasks);
        tomorrowTasksItemAdapter.setEventListener(new TaskEventsListener(tomorrowTasks));
        tomorrowTasksWrappedAdapter = tomorrowTasksRecyclerViewSwipeManager.createWrappedAdapter(tomorrowTasksItemAdapter);
        tomorrowTasksAnimator = new SwipeDismissItemAnimator();
        tomorrowTasksAnimator.setSupportsChangeAnimations(false);
        tomorrowTasksRecyclerView.setLayoutManager(tomorrowTasksLayoutManager);
        tomorrowTasksRecyclerView.setAdapter(tomorrowTasksWrappedAdapter);
        tomorrowTasksRecyclerView.setItemAnimator(tomorrowTasksAnimator);
        if (!supportsViewElevation()) {
            tomorrowTasksRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        tomorrowTasksRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        tomorrowTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(tomorrowTasksRecyclerView);
        tomorrowTasksRecyclerViewSwipeManager.attachRecyclerView(tomorrowTasksRecyclerView);

        // InTheNextSevenDays Tasks
        inTheNextSevenDaysTasksRecyclerView = (RecyclerView) view.findViewById(R.id.inTheNextSevenDaysTasksRecyclerView);
        inTheNextSevenDaysTasksLayoutManager = new CustomLinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        inTheNextSevenDaysTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        inTheNextSevenDaysTasksItemAdapter = new SwipeableTaskAdapter(inTheNextSevenDaysTasks);
        inTheNextSevenDaysTasksItemAdapter.setEventListener(new TaskEventsListener(inTheNextSevenDaysTasks));
        inTheNextSevenDaysTasksWrappedAdapter = inTheNextSevenDaysTasksRecyclerViewSwipeManager.createWrappedAdapter(inTheNextSevenDaysTasksItemAdapter);
        inTheNextSevenDaysTasksAnimator = new SwipeDismissItemAnimator();
        inTheNextSevenDaysTasksAnimator.setSupportsChangeAnimations(false);
        inTheNextSevenDaysTasksRecyclerView.setLayoutManager(inTheNextSevenDaysTasksLayoutManager);
        inTheNextSevenDaysTasksRecyclerView.setAdapter(inTheNextSevenDaysTasksWrappedAdapter);
        inTheNextSevenDaysTasksRecyclerView.setItemAnimator(inTheNextSevenDaysTasksAnimator);
        if (!supportsViewElevation()) {
            inTheNextSevenDaysTasksRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        inTheNextSevenDaysTasksRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(inTheNextSevenDaysTasksRecyclerView);
        inTheNextSevenDaysTasksRecyclerViewSwipeManager.attachRecyclerView(inTheNextSevenDaysTasksRecyclerView);

        // Later Tasks
        laterTasksRecyclerView = (RecyclerView) view.findViewById(R.id.laterTasksRecyclerView);
        laterTasksLayoutManager = new CustomLinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);
        laterTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        laterTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        laterTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        laterTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        laterTasksItemAdapter = new SwipeableTaskAdapter(laterTasks);
        laterTasksItemAdapter.setEventListener(new TaskEventsListener(laterTasks));
        laterTasksWrappedAdapter = laterTasksRecyclerViewSwipeManager.createWrappedAdapter(laterTasksItemAdapter);
        laterTasksAnimator = new SwipeDismissItemAnimator();
        laterTasksAnimator.setSupportsChangeAnimations(false);
        laterTasksRecyclerView.setLayoutManager(laterTasksLayoutManager);
        laterTasksRecyclerView.setAdapter(laterTasksWrappedAdapter);
        laterTasksRecyclerView.setItemAnimator(laterTasksAnimator);
        if (!supportsViewElevation()) {
            laterTasksRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        laterTasksRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        laterTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(laterTasksRecyclerView);
        laterTasksRecyclerViewSwipeManager.attachRecyclerView(laterTasksRecyclerView);

        displayTasks();

        // Setup TaskAdder
        newTaskClearButton = (ImageView) view.findViewById(R.id.newTaskClearButton);
        taskAdderSendButton = (ImageView) view.findViewById(R.id.taskAdderSendButton);
        previewTaskLayout = (LinearLayout) view.findViewById(R.id.previewTaskLayout);
        taskAdderInput = (MaterialEditText) view.findViewById(R.id.taskAdderInput);
        taskPrimaryText = (TextView) view.findViewById(R.id.taskPrimaryText);
        taskSecondaryText = (TextView) view.findViewById(R.id.taskSecondaryText);
        taskSecondaryText.setText((new Date()).toLocaleString());
        taskAdderInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (!text.equals("")) {
                    newTask.setTitle(text);
                    renderPreviewTask();
                } else {
                    previewTaskLayout.setVisibility(View.GONE);
                }
            }
        });
        taskAdderInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    insertTask();
                    handled = true;
                }
                return handled;
            }
        });
        newTaskClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destroyPreviewTask();
            }
        });
        taskAdderSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertTask();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Main = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
        if (context instanceof MainActivity){
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int key = Integer.valueOf(v.getTag().toString());
        tagsFilteringDialog.dismiss();
        setCurrentTag(tags.get(key));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("TasksFragment", "onItemClick (line 319): Write a log message" + i);
    }

    public interface OnFragmentInteractionListener {
        void toast(String str);
    }

    // Data
    private void fetchTasks(Tag tag) {
        overdueTasks.clear();
        todayTasks.clear();
        tomorrowTasks.clear();
        inTheNextSevenDaysTasks.clear();
        laterTasks.clear();

        User user = app.getCurrentUser();

        if (user == null) {
            return;
        }

        overdueTasks.addAll(user.getOverdueTasks(tag));
        todayTasks.addAll(user.getTodayTasks(tag));
        tomorrowTasks.addAll(user.getTomorrowTasks(tag));
        inTheNextSevenDaysTasks.addAll(user.getInTheNextSevenDaysTasks(tag));
        laterTasks.addAll(user.getLaterTasks(tag));
    }

    private void insertTask() {
        /*MeteorSingleton.getInstance().insert("Tasks", newTask.getObject(), new ResultListener() {
            @Override
            public void onSuccess(String result) {
                showSnack(String.format("'%s' added", newTask.getTitle()));
                destroyPreviewTask();
            }

            @Override
            public void onError(String error, String reason, String details) {
                showSnack("Error adding the task. Try again");
            }
        });*/
    }

    private void postponeAllTheseTasks(List<Task> tasks) {
        for (Task task : tasks) {
            //task.postponeToNextDayServer();
        }
    }

    private void markAsDoneAllTheseTasks(List<Task> tasks) {
        for (Task task : tasks) {
            //task.markAsDoneServer();
        }
    }

    // Display

    private void displayTasks() {
        noTasksLayout.setVisibility(View.VISIBLE);
        if (overdueTasks.size() > 0) {
            overdueTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        } else {
            overdueTasksLayout.setVisibility(View.GONE);
        }
        if (todayTasks.size() > 0) {
            todayTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        } else {
            todayTasksLayout.setVisibility(View.GONE);
        }
        if (tomorrowTasks.size() > 0) {
            tomorrowTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        } else {
            tomorrowTasksLayout.setVisibility(View.GONE);
        }
        if (inTheNextSevenDaysTasks.size() > 0) {
            inTheNextSevenDaysTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        } else {
            inTheNextSevenDaysTasksLayout.setVisibility(View.GONE);
        }
        if (laterTasks.size() > 0) {
            laterTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        } else {
            laterTasksLayout.setVisibility(View.GONE);
        }

        overdueTasksWrappedAdapter.notifyDataSetChanged();
        overdueTasksItemAdapter.notifyDataSetChanged();
        todayTasksWrappedAdapter.notifyDataSetChanged();
        todayTasksItemAdapter.notifyDataSetChanged();
        tomorrowTasksWrappedAdapter.notifyDataSetChanged();
        tomorrowTasksItemAdapter.notifyDataSetChanged();
        inTheNextSevenDaysTasksWrappedAdapter.notifyDataSetChanged();
        inTheNextSevenDaysTasksItemAdapter.notifyDataSetChanged();
        laterTasksWrappedAdapter.notifyDataSetChanged();
        laterTasksItemAdapter.notifyDataSetChanged();

        //WrapperAdapterUtils.releaseAll(todayTasksWrappedAdapter);
        //todayTasksRecyclerViewSwipeManager.release();
        //todayTasksItemAdapter = new SwipeableTaskAdapter(todayTasks);
        //todayTasksWrappedAdapter = todayTasksRecyclerViewSwipeManager.createWrappedAdapter(todayTasksItemAdapter);
        //todayTasksRecyclerView.swapAdapter(todayTasksWrappedAdapter, false);
        //Log.d("TasksFragment@603", todayTasks.toString());
    }

    public void reRenderTasks() {
        fetchTasks(currentTag);
        displayTasks();
    }

    public void setCurrentTag(Tag tag) {
        currentTag = tag;
        fetchTasks(currentTag);
        displayTasks();

        if (tag == null) {
            return;
        }

        currentTagChipLayout.setVisibility(View.VISIBLE);
        currentTagChipLabel.setText(currentTag.getHashName());
        IconicsDrawable tagIcon = new IconicsDrawable(getContext()).sizeDp(13).color(Color.WHITE);
        int hintColor = getContext().getResources().getColor(R.color.colorHintText);

        if (tag.getColor() != null) {
            currentTagChipIcon.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
        } else {
            currentTagChipIcon.setBackgroundDrawable(
                    Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
            );
        }

        if (tag.getIcon() != null) {
            try {
                tagIcon.icon(GoogleMaterial.Icon.valueOf("gmd_" + tag.getIcon()));
            } catch (Exception e ) {
                tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            }
        } else {
            tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
        }

        currentTagChipIcon.setImageDrawable(tagIcon);

        currentTagChipIconDelete.setBackgroundDrawable(Graphics.createRoundDrawable("#8C000000"));
        currentTagChipIconDelete.setImageDrawable(
                new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_clear)
                    .sizeDp(9)
                    .color(Color.parseColor("#D4D4D4"))
        );
        currentTagChipIconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCurrentTag();
            }
        });
        
    }

    public void clearCurrentTag() {
        currentTagChipLayout.setVisibility(View.GONE);
        setCurrentTag(null);
    }

    private void renderPreviewTask() {
        previewTaskLayout.setVisibility(View.VISIBLE);
        taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        taskPrimaryText.setText(newTask.getTitle());
        taskSecondaryText.setText(newTask.getDueDate().toLocaleString());
    }

    private void destroyPreviewTask() {
        if (isAdded()) {
            Tools.hideKeyboard(mainActivity);
            newTask = new Task();
            previewTaskLayout.setVisibility(View.GONE);
            taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorHintTextNonFaded), PorterDuff.Mode.SRC_IN);
            taskPrimaryText.setText("");
            taskAdderInput.setText("");
            taskSecondaryText.setText(new Date().toLocaleString());
            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
        }
    }

    public void showTagsFilteringDialog() {
        tagsFilteringDialog.show();
    }

    public void showSnack(String text) {
        Log.d("TasksFragment", "showSnack (line 599): Write a log message" + text);
        Snackbar.make(tasksCoordinatorLayout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

}


