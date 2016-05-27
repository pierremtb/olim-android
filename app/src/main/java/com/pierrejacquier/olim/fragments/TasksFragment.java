package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.adapters.TasksAdapter;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.helpers.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.db.Document;

import com.pierrejacquier.olim.helpers.OnStartDragListener;
import com.pierrejacquier.olim.helpers.SimpleItemTouchHelperCallback;

public class TasksFragment
        extends Fragment
        implements View.OnClickListener,
            OnStartDragListener {

    private Olim app;
    private OnFragmentInteractionListener Main;

    private ArrayList<Task> overdueTasks = new ArrayList<>();
    private ArrayList<Task> todayTasks = new ArrayList<>();
    private ArrayList<Task> tomorrowTasks = new ArrayList<>();
    private ArrayList<Task> inTheNextSevenDaysTasks = new ArrayList<>();
    private ArrayList<Task> laterTasks = new ArrayList<>();

    private ArrayList<Tag> tags = new ArrayList<>();

    private ItemTouchHelper mItemTouchHelper;

    public TasksFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getActivity().getApplicationContext();
        if (MeteorSingleton.getInstance().isLoggedIn()) {
            fetchTasks();
            fetchTags();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_tasks, container, false);
        LinearLayout overdueTasksLayout = (LinearLayout) myView.findViewById(R.id.overdueTasksLayout);
        LinearLayout todayTasksLayout = (LinearLayout) myView.findViewById(R.id.todayTasksLayout);
        LinearLayout tomorrowTasksLayout = (LinearLayout) myView.findViewById(R.id.tomorrowTasksLayout);
        LinearLayout inTheNextSevenDaysTasksLayout = (LinearLayout) myView.findViewById(R.id.inTheNextSevenDaysTasksLayout);
        LinearLayout laterTasksLayout = (LinearLayout) myView.findViewById(R.id.laterTasksLayout);
        LinearLayout noTasksLayout = (LinearLayout) myView.findViewById(R.id.noTasksLayout);
        if (overdueTasks.size() > 0) {
            overdueTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        }
        if (todayTasks.size() > 0) {
            todayTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        }
        if (tomorrowTasks.size() > 0) {
            tomorrowTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        }
        if (inTheNextSevenDaysTasks.size() > 0) {
            inTheNextSevenDaysTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        }
        if (laterTasks.size() > 0) {
            laterTasksLayout.setVisibility(View.VISIBLE);
            noTasksLayout.setVisibility(View.GONE);
        }
        return myView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTasksRecyclerView(view, overdueTasks, R.id.overdueTasksRecyclerView, this);
        setupTasksRecyclerView(view, todayTasks, R.id.todayTasksRecyclerView, this);
        setupTasksRecyclerView(view, tomorrowTasks, R.id.tomorrowTasksRecyclerView, this);
        setupTasksRecyclerView(view, inTheNextSevenDaysTasks, R.id.inTheNextSevenDaysTasksRecyclerView, this);
        setupTasksRecyclerView(view, laterTasks, R.id.laterTasksRecyclerView, this);
    }

    private void setupTasksRecyclerView(View view, ArrayList<Task> tasks, int id, OnStartDragListener dragStartListener) {

        TasksAdapter adapter = new TasksAdapter(dragStartListener, tasks);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(id);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
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
        int id = v.getId();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public interface OnFragmentInteractionListener {
        void toast(String str);
    }

    // Data
    private void fetchTasks() {
        Document[] tasks = MeteorSingleton.getInstance()
                .getDatabase()
                .getCollection("Tasks")
                .whereEqual("owner", MeteorSingleton.getInstance().getUserId()).find();

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

        for (Document taskDoc : tasks) {
            Task task = new Task(taskDoc);
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

    private void fetchTags() {
        Document[] tagsDocs = MeteorSingleton.getInstance()
                .getDatabase()
                .getCollection("Tags")
                .whereEqual("owner", MeteorSingleton.getInstance().getUserId()).find();
        for (Document tag : tagsDocs) {
            tags.add(new Tag(tag));
        }
    }
}


