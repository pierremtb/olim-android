package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.pierrejacquier.olim.activities.MainActivity;
import com.pierrejacquier.olim.adapters.SwipeableTaskAdapter;
import com.pierrejacquier.olim.adapters.TagsAdapter;
import com.pierrejacquier.olim.adapters.TagsListAdapter;
import com.pierrejacquier.olim.data.User;
import com.pierrejacquier.olim.helpers.CustomLinearLayoutManager;
import com.rengwuxian.materialedittext.MaterialEditText;

import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.helpers.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.db.Document;

public class TasksFragment
        extends Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    Olim app;
    private OnFragmentInteractionListener Main;

    private ArrayList<Task> overdueTasks = new ArrayList<>();
    private ArrayList<Task> todayTasks = new ArrayList<>();
    private ArrayList<Task> tomorrowTasks = new ArrayList<>();
    private ArrayList<Task> inTheNextSevenDaysTasks = new ArrayList<>();
    private ArrayList<Task> laterTasks = new ArrayList<>();

    private List<Tag> tags = new ArrayList<>();

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
    
    private RecyclerView.Adapter overdueTasksAdapter;
    private RecyclerView.Adapter todayTasksAdapter;
    private RecyclerView.Adapter tomorrowTasksAdapter;
    private RecyclerView.Adapter inTheNextSevenDaysTasksAdapter;
    private RecyclerView.Adapter laterTasksAdapter;
    

    public MaterialDialog tagsFilteringDialog;

    private Task newTask = new Task();

    public TasksFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getActivity().getApplicationContext();
        if (MeteorSingleton.getInstance().isLoggedIn()) {
            Document userDoc = MeteorSingleton.getInstance().getDatabase()
                                .getCollection("users")
                                .getDocument(MeteorSingleton.getInstance().getUserId());
            if (userDoc != null) {
                app.setCurrentUser(new User(userDoc));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_tasks, container, false);
        overdueTasksLayout = (LinearLayout) myView.findViewById(R.id.overdueTasksLayout);
        todayTasksLayout = (LinearLayout) myView.findViewById(R.id.todayTasksLayout);
        tomorrowTasksLayout = (LinearLayout) myView.findViewById(R.id.tomorrowTasksLayout);
        inTheNextSevenDaysTasksLayout = (LinearLayout) myView.findViewById(R.id.inTheNextSevenDaysTasksLayout);
        laterTasksLayout = (LinearLayout) myView.findViewById(R.id.laterTasksLayout);
        noTasksLayout = (LinearLayout) myView.findViewById(R.id.noTasksLayout);

        overdueTasksLayout = (LinearLayout) myView.findViewById(R.id.overdueTasksLayout);
        todayTasksLayout = (LinearLayout) myView.findViewById(R.id.todayTasksLayout);
        tomorrowTasksLayout = (LinearLayout) myView.findViewById(R.id.tomorrowTasksLayout);
        inTheNextSevenDaysTasksLayout = (LinearLayout) myView.findViewById(R.id.inTheNextSevenDaysTasksLayout);
        laterTasksLayout = (LinearLayout) myView.findViewById(R.id.laterTasksLayout);
        noTasksLayout = (LinearLayout) myView.findViewById(R.id.noTasksLayout);
        if (MeteorSingleton.getInstance().isLoggedIn()) {
            tags = app.getCurrentUser().getTags();
            fetchTasks(null);
            displayTasks();
        }
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

        tagsFilteringDialog = new MaterialDialog.Builder(getActivity())
                .title("Tags")
                .autoDismiss(true)
                .positiveText(R.string.clear)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        fetchTasks(null);
                        displayTasks();
                    }
                })
                .adapter(new TagsListAdapter(
                            getContext(),
                            tags.toArray(new Tag[tags.size()]), this),
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                Log.d("TasksFragment", "onSelection (line 164): Write a log message" + which);
                            }
                        })
                .build();
        ListView list = tagsFilteringDialog.getListView();
        list.setOnItemClickListener(this);

        overdueTasksRecyclerView = (RecyclerView) view.findViewById(R.id.overdueTasksRecyclerView);
        todayTasksRecyclerView = (RecyclerView) view.findViewById(R.id.todayTasksRecyclerView);
        tomorrowTasksRecyclerView = (RecyclerView) view.findViewById(R.id.tomorrowTasksRecyclerView);
        inTheNextSevenDaysTasksRecyclerView = (RecyclerView) view.findViewById(R.id.inTheNextSevenDaysTasksRecyclerView);
        laterTasksRecyclerView = (RecyclerView) view.findViewById(R.id.laterTasksRecyclerView);


        // Setup TasksGroups
        setupTasksRecyclerView(overdueTasks, overdueTasksRecyclerView);
        setupTasksRecyclerView(todayTasks, todayTasksRecyclerView);
        setupTasksRecyclerView(tomorrowTasks, tomorrowTasksRecyclerView);
        setupTasksRecyclerView(inTheNextSevenDaysTasks, inTheNextSevenDaysTasksRecyclerView);
        setupTasksRecyclerView(laterTasks, laterTasksRecyclerView);

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

    private void setupTasksRecyclerView(final ArrayList<Task> tasks, RecyclerView  mRecyclerView) {
        if (tasks.size() == 0) {
            return;
        }

        CustomLinearLayoutManager mLayoutManager;
        RecyclerViewSwipeManager mRecyclerViewSwipeManager;
        RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

        mLayoutManager = new CustomLinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false);

        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        final SwipeableTaskAdapter myItemAdapter = new SwipeableTaskAdapter(tasks);
        myItemAdapter.setEventListener(new SwipeableTaskAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                Task task = tasks.get(position);
                Log.d("ToggleDone", task.toString());
                task.toggleDoneServer();
            }

            @Override
            public void onItemPinned(int position) {
                Log.d("Pinned", position + "");
            }

            @Override
            public void onItemViewClicked(View v, boolean pinned) {
                Log.d("Clicekd", v.toString());
            }
        });

        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);

        if (!supportsViewElevation()) {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Main = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int key = Integer.valueOf(v.getTag().toString());
        Log.d("TasksFragment", "onClick (line 309): " + tags.get(key).getHashName());
        tagsFilteringDialog.dismiss();
        Log.d("TasksFragment", "onClick (line 319): tag" + tags.get(key).toString());
        fetchTasks(tags.get(key));
        Log.d("TasksFragment", "onClick (line 321): Write a log messageauui" + overdueTasks.toString());
        displayTasks();
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
        List<Task> tasks;
        if (tag == null) {
            tasks = app.getCurrentUser().getTasks();
        } else {
            tasks = app.getCurrentUser().getTasksByTag(tag);
            Log.d("TasksFragment", "fetchTasks (line 341): Write a log message" + tasks.toString());
        }

        Calendar dueDate = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        Calendar inTheNextSevenDaysStart = Calendar.getInstance();
        Calendar inTheNextSevenDaysEnd = Calendar.getInstance();
        Tools.setStartOfDay(today);
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        inTheNextSevenDaysStart.add(Calendar.DAY_OF_MONTH, 2);
        inTheNextSevenDaysEnd.add(Calendar.DAY_OF_MONTH, 9);
        Tools.setStartOfDay(inTheNextSevenDaysStart);
        Tools.setStartOfDay(inTheNextSevenDaysEnd);

        overdueTasks.clear();
        todayTasks.clear();
        tomorrowTasks.clear();
        inTheNextSevenDaysTasks.clear();
        laterTasks.clear();

        for (Task task : tasks) {
            dueDate.setTime(task.getDueDate());

            if (today.after(dueDate)) {
                overdueTasks.add(task);
            } else if (today.get(Calendar.DAY_OF_MONTH) == dueDate.get(Calendar.DAY_OF_MONTH)) {
                todayTasks.add(task);
            } else if (tomorrow.get(Calendar.DAY_OF_MONTH) == dueDate.get(Calendar.DAY_OF_MONTH)) {
                tomorrowTasks.add(task);
            } else if (inTheNextSevenDaysStart.before(dueDate) && inTheNextSevenDaysEnd.after(dueDate)) {
                inTheNextSevenDaysTasks.add(task);
            } else {
                laterTasks.add(task);
            }
        }
    }

    private void insertTask() {
        newTask.setCreatedAt(new Date());
        MeteorSingleton.getInstance().insert("Tasks", newTask.getObject(), new ResultListener() {
            @Override
            public void onSuccess(String result) {
                Log.d("auie", "Succes");
            }

            @Override
            public void onError(String error, String reason, String details) {
                Log.d("Error", error);
                Log.d("Error", reason);
            }
        });
        Log.d("NT: ", newTask.getObject().toString());
        destroyPreviewTask();
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
    }

    private void renderPreviewTask() {
        newTask.setOwner(MeteorSingleton.getInstance().getUserId());
        previewTaskLayout.setVisibility(View.VISIBLE);
        taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        taskPrimaryText.setText(newTask.getTitle());
        taskSecondaryText.setText(newTask.getDueDate().toLocaleString());
    }

    private void destroyPreviewTask() {
        newTask = new Task();
        previewTaskLayout.setVisibility(View.GONE);
        taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorHintTextNonFaded), PorterDuff.Mode.SRC_IN);
        taskPrimaryText.setText("");
        taskSecondaryText.setText(new Date().toLocaleString());
    }

    public void showTagsFilteringDialog() {
        tagsFilteringDialog.show();
    }


    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

}


