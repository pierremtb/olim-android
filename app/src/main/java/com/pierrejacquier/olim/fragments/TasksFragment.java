package com.pierrejacquier.olim.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.activities.MainActivity;
import com.pierrejacquier.olim.activities.TagActivity;
import com.pierrejacquier.olim.activities.TaskActivity;
import com.pierrejacquier.olim.adapters.SwipeableTaskAdapter;
import com.pierrejacquier.olim.adapters.TagsListAdapter;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.data.User;
import com.pierrejacquier.olim.databinding.FragmentTasksBinding;
import com.pierrejacquier.olim.helpers.CustomLinearLayoutManager;
import com.pierrejacquier.olim.helpers.Graphics;
import com.pierrejacquier.olim.helpers.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksFragment
        extends Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Olim app;
    private OnFragmentInteractionListener Main;
    private MainActivity mainActivity;
    private FragmentTasksBinding binding;

    private ArrayList<Task> overdueTasks = new ArrayList<>();
    private ArrayList<Task> todayTasks = new ArrayList<>();
    private ArrayList<Task> tomorrowTasks = new ArrayList<>();
    private ArrayList<Task> inTheNextSevenDaysTasks = new ArrayList<>();
    private ArrayList<Task> laterTasks = new ArrayList<>();
    private Task newTask = new Task("New task", new Date());

    private List<Tag> tags = new ArrayList<>();
    private Tag currentTag = null;

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

    private MaterialDialog tagsFilteringDialog;
    private MaterialDialog tagChooserDialog;
    private DatePickerDialog newTaskDueDatePickerDialog;
    private TimePickerDialog newTaskDueTimePickerDialog;

    public TasksFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tasks, container, false);

        binding.postponeAllOverdueTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(overdueTasks);
            }
        });
        binding.postponeAllTodayTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(todayTasks);
            }
        });
        binding.postponeAllTomorrowTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(tomorrowTasks);
            }
        });
        binding.postponeAllInTheNextSevenDaysTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(inTheNextSevenDaysTasks);
            }
        });
        binding.postponeAllLaterTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(laterTasks);
            }
        });
        
        binding.markAsDoneAllOverdueTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(overdueTasks);
            }
        });
        binding.markAsDoneAllTodayTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(todayTasks);
            }
        });
        binding.markAsDoneAllTomorrowTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(tomorrowTasks);
            }
        });
        binding.markAsDoneAllInTheNextSevenDaysTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(inTheNextSevenDaysTasks);
            }
        });
        binding.markAsDoneAllLaterTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(laterTasks);
            }
        });

        tags = app.getCurrentUser().getTags();
        Main.updateUserTasks();
        fetchTasks(null);

        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Main.refreshData();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tasksNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // TODO: Set appBar elevation if scrolled
            }
        });
        final List<Tag> tags = Main.updateUserTags();

        createNewTaskTagChooserDialog(getContext());
        createTagsFilteringDialog(getContext());

        // Overdue Tasks
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
        binding.overdueTasksRecyclerView.setLayoutManager(overdueTasksLayoutManager);
        binding.overdueTasksRecyclerView.setAdapter(overdueTasksWrappedAdapter);
        binding.overdueTasksRecyclerView.setItemAnimator(overdueTasksAnimator);
        overdueTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.overdueTasksRecyclerView);
        overdueTasksRecyclerViewSwipeManager.attachRecyclerView(binding.overdueTasksRecyclerView);

        // Today Tasks
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
        binding.todayTasksRecyclerView.setLayoutManager(todayTasksLayoutManager);
        binding.todayTasksRecyclerView.setAdapter(todayTasksWrappedAdapter);
        binding.todayTasksRecyclerView.setItemAnimator(todayTasksAnimator);
        todayTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.todayTasksRecyclerView);
        todayTasksRecyclerViewSwipeManager.attachRecyclerView(binding.todayTasksRecyclerView);

        // Tomorrow Tasks
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
        binding.tomorrowTasksRecyclerView.setLayoutManager(tomorrowTasksLayoutManager);
        binding.tomorrowTasksRecyclerView.setAdapter(tomorrowTasksWrappedAdapter);
        binding.tomorrowTasksRecyclerView.setItemAnimator(tomorrowTasksAnimator);
        tomorrowTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.tomorrowTasksRecyclerView);
        tomorrowTasksRecyclerViewSwipeManager.attachRecyclerView(binding.tomorrowTasksRecyclerView);

        // InTheNextSevenDays Tasks
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
        binding.inTheNextSevenDaysTasksRecyclerView.setLayoutManager(inTheNextSevenDaysTasksLayoutManager);
        binding.inTheNextSevenDaysTasksRecyclerView.setAdapter(inTheNextSevenDaysTasksWrappedAdapter);
        binding.inTheNextSevenDaysTasksRecyclerView.setItemAnimator(inTheNextSevenDaysTasksAnimator);
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.inTheNextSevenDaysTasksRecyclerView);
        inTheNextSevenDaysTasksRecyclerViewSwipeManager.attachRecyclerView(binding.inTheNextSevenDaysTasksRecyclerView);

        // Later Tasks
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
        binding.laterTasksRecyclerView.setLayoutManager(laterTasksLayoutManager);
        binding.laterTasksRecyclerView.setAdapter(laterTasksWrappedAdapter);
        binding.laterTasksRecyclerView.setItemAnimator(laterTasksAnimator);
        laterTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.laterTasksRecyclerView);
        laterTasksRecyclerViewSwipeManager.attachRecyclerView(binding.laterTasksRecyclerView);

        displayTasks();

        // Setup TaskAdder
        binding.taskSecondaryText.setText((new Date()).toLocaleString());
        binding.taskAdderInput.addTextChangedListener(new TextWatcher() {
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
                    binding.previewTaskLayout.setVisibility(View.GONE);
                }
            }
        });
        binding.taskAdderInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        binding.newTaskClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destroyPreviewTask();
            }
        });
        binding.taskAdderSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertTask();
            }
        });
        binding.newTaskTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.updateUserTags();
                List<Tag> tags = app.getCurrentUser().getTags();
                tagChooserDialog.show();
            }
        });
        binding.newTaskChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar taskDueDate = Calendar.getInstance();
                taskDueDate.setTime(newTask.getDueDate());
                int taskYear = taskDueDate.get(Calendar.YEAR);
                int taskMonth = taskDueDate.get(Calendar.MONTH);
                int taskDay = taskDueDate.get(Calendar.DAY_OF_MONTH);
                newTaskDueDatePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int month, int day) {
                                newTask.setDueDate(year, month, day);
                                binding.setNewTask(newTask);

                            }
                        }, taskYear, taskMonth, taskDay);
                newTaskDueDatePickerDialog.show();
            }
        });
        binding.newTaskChooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar taskDueDate = Calendar.getInstance();
                taskDueDate.setTime(newTask.getDueDate());
                int taskHour = taskDueDate.get(Calendar.HOUR_OF_DAY);
                int taskMinute = taskDueDate.get(Calendar.MINUTE);
                newTaskDueTimePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour,
                                                  int minute) {
                                newTask.setDueDate(hour, minute);
                                binding.setNewTask(newTask);

                            }
                        }, taskHour, taskMinute, DateFormat.is24HourFormat(getContext()));
                newTaskDueTimePickerDialog.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        reRenderTasks();
        Main.updateUserTags();
        createNewTaskTagChooserDialog(getContext());
        if (resultCode == 1) {
            tags = Main.updateUserTags();
            createTagsFilteringDialog(getContext());
            tagsFilteringDialog.show();
        }

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
        Tag tag = (Tag) v.getTag();
        if (getResources().getResourceName(v.getId()).equals("com.pierrejacquier.olim:id/tagEdit")) {
            launchTagActivity(tag.getId());
            tagsFilteringDialog.dismiss();
        } else {
            if (tagsFilteringDialog.isShowing()) {
                tagsFilteringDialog.dismiss();
                setCurrentTag(tag);
            } else if (tagChooserDialog.isShowing()) {
                tagChooserDialog.dismiss();
                newTask.setTag(tag);
                renderNewTask();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }
    public interface OnFragmentInteractionListener {
        void insertTask(Task task);

        void updateTask(Task task);

        List<Task> updateUserTasks();

        List<Tag> updateUserTags();

        Tag getTag(long id);

        void refreshData();

        List<Task> getTasks(Tag currentTag);

        List<Tag> getTags();
    }

    /**
     * Data handling methods
     */

    private void fetchTasks(Tag tag) {
        app.getCurrentUser().setTags(Main.getTags());

        overdueTasks.clear();
        todayTasks.clear();
        tomorrowTasks.clear();
        inTheNextSevenDaysTasks.clear();
        laterTasks.clear();

        User user = app.getCurrentUser();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean excludeDone = !prefs.getBoolean("display_done", true);

        if (user == null) {
            return;
        }

        overdueTasks.addAll(user.getOverdueTasks(tag, true));
        todayTasks.addAll(user.getTodayTasks(tag, excludeDone));
        tomorrowTasks.addAll(user.getTomorrowTasks(tag, excludeDone));
        inTheNextSevenDaysTasks.addAll(user.getInTheNextSevenDaysTasks(tag, excludeDone));
        laterTasks.addAll(user.getLaterTasks(tag, excludeDone));
    }

    private void insertTask() {
        Main.insertTask(newTask);
        showSnack(String.format("'%s' added", newTask.getTitle()));
        destroyPreviewTask();
    }

    private void postponeAllTheseTasks(List<Task> tasks) {
        for (Task task : tasks) {
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(task.getDueDate());
            dueDate.add(Calendar.DAY_OF_MONTH, 1);
            task.setDueDate(dueDate.getTime());
            Main.updateTask(task);
        }
    }

    private void markAsDoneAllTheseTasks(List<Task> tasks) {
        for (Task task : tasks) {
            task.setDone(true);
            Main.updateTask(task);
        }
    }

    /**
     * Layout handling methods
     */

    private void renderNewTask() {
        binding.setNewTask(newTask);
        IconicsDrawable id = new IconicsDrawable(getContext()).sizeDp(20).color(Color.WHITE);
        if (newTask.getTag() == null) {
            int color = getContext().getResources().getColor(R.color.colorHintText);
            binding.newTaskTag.setBackgroundDrawable(Graphics.createRoundDrawable(color));
            binding.newTaskTag.setImageDrawable(id.icon("gmd_label_outline"));
        } else {
            binding.newTaskTag.setBackgroundDrawable(Graphics.createRoundDrawable(newTask.getTag().getColor()));
            binding.newTaskTag.setImageDrawable(id.icon(newTask.getTag().getIconicsName()));
        }
    }

    private void createTagsFilteringDialog(Context context) {
        tagsFilteringDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.filter_with_tag)
                .autoDismiss(true)
                .positiveText(R.string.clear)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        clearCurrentTag();
                    }
                })
                .negativeText(R.string.create_tag)
                .negativeColor(getResources().getColor(R.color.colorPrimaryText))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchTagActivity(-1);
                        tagsFilteringDialog.dismiss();
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
    }

    private void createNewTaskTagChooserDialog(Context context) {
        tags = Main.updateUserTags();
        tagChooserDialog = new MaterialDialog.Builder(context)
                .title(R.string.change_tag)
                .autoDismiss(true)
                .positiveText(R.string.clear)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        newTask.setTag(null);
                        renderNewTask();
                    }
                })
                .negativeText(R.string.create_tag)
                .negativeColor(getResources().getColor(R.color.colorPrimaryText))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchTagActivity(-1);
                    }
                })
                .adapter(new TagsListAdapter(context, tags.toArray(new Tag[tags.size()]), this), null)
                .build();
    }

    public void endRefreshing() {
        binding.swipeRefresh.setRefreshing(false);
    }

    private void displayTasks() {
        binding.noTasksLayout.setVisibility(View.VISIBLE);
        if (overdueTasks.size() > 0) {
            binding.overdueTasksLayout.setVisibility(View.VISIBLE);
            binding.noTasksLayout.setVisibility(View.GONE);
        } else {
            binding.overdueTasksLayout.setVisibility(View.GONE);
        }
        if (todayTasks.size() > 0) {
            binding.todayTasksLayout.setVisibility(View.VISIBLE);
            binding.noTasksLayout.setVisibility(View.GONE);
        } else {
            binding.todayTasksLayout.setVisibility(View.GONE);
        }
        if (tomorrowTasks.size() > 0) {
            binding.tomorrowTasksLayout.setVisibility(View.VISIBLE);
            binding.noTasksLayout.setVisibility(View.GONE);
        } else {
            binding.tomorrowTasksLayout.setVisibility(View.GONE);
        }
        if (inTheNextSevenDaysTasks.size() > 0) {
            binding.inTheNextSevenDaysTasksLayout.setVisibility(View.VISIBLE);
            binding. noTasksLayout.setVisibility(View.GONE);
        } else {
            binding.inTheNextSevenDaysTasksLayout.setVisibility(View.GONE);
        }
        if (laterTasks.size() > 0) {
            binding.laterTasksLayout.setVisibility(View.VISIBLE);
            binding.noTasksLayout.setVisibility(View.GONE);
        } else {
            binding.laterTasksLayout.setVisibility(View.GONE);
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
    }

    private void reRenderTasks() {
        Main.updateUserTasks();
        Main.updateUserTags();
        fetchTasks(currentTag);
        displayTasks();
    }

    public void reRenderTasksTemp() {
        fetchTasks(currentTag);
        displayTasks();
    }

    public Tag getCurrentTag() {
        return currentTag;
    }

    private void setCurrentTag(Tag tag) {
        currentTag = tag;
        app.getCurrentUser().setTasks(Main.getTasks(currentTag));
        fetchTasks(currentTag);
        displayTasks();

        if (tag == null) {
            return;
        }

        binding.currentTagChipLayout.setVisibility(View.VISIBLE);
        binding.currentTagChipLabel.setText(currentTag.getHashName());
        IconicsDrawable tagIcon = new IconicsDrawable(getContext()).sizeDp(13).color(Color.WHITE);
        int hintColor = getContext().getResources().getColor(R.color.colorHintText);

        if (tag.getColor() != null) {
            binding.currentTagChipIcon.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
        } else {
            binding.currentTagChipIcon.setBackgroundDrawable(
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

        binding.currentTagChipIcon.setImageDrawable(tagIcon);

        binding.currentTagChipIconDelete.setBackgroundDrawable(Graphics.createRoundDrawable("#8C000000"));
        binding.currentTagChipIconDelete.setImageDrawable(
                new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_clear)
                    .sizeDp(9)
                    .color(Color.parseColor("#D4D4D4"))
        );
        binding.currentTagChipIconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCurrentTag();
            }
        });
    }

    private void clearCurrentTag() {
        binding.currentTagChipLayout.setVisibility(View.GONE);
        setCurrentTag(null);
    }

    public void hideTaskAdder() {
        binding.taskAdderCard.setVisibility(View.GONE);
    }

    public void showTaskAdder() {
        binding.taskAdderCard.setVisibility(View.VISIBLE);
    }

    private void renderPreviewTask() {
        binding.previewTaskLayout.setVisibility(View.VISIBLE);
        binding.taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
//        binding.taskPrimaryText.setText(newTask.getTitle());
//        binding.taskSecondaryText.setText(newTask.getDueDate().toLocaleString());
        binding.setNewTask(newTask);
    }

    private void destroyPreviewTask() {
        Tools.hideKeyboard(mainActivity);
        newTask = new Task("New task", new Date());
        binding.previewTaskLayout.setVisibility(View.GONE);
        binding.taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorHintTextNonFaded), PorterDuff.Mode.SRC_IN);
        binding.taskPrimaryText.setText("");
        binding.taskAdderInput.setText("");
        binding.taskSecondaryText.setText(new Date().toLocaleString());
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    public void showTagsFilteringDialog() {
        tagsFilteringDialog.show();
    }

    public void showSnack(String text) {
        Snackbar.make(binding.tasksCoordinatorLayout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /**
     * Navigation
     */

    private void launchTaskActivity(long id) {
        Intent intent = new Intent(getActivity(), TaskActivity.class);
        Bundle b = new Bundle();
        b.putLong("id", id);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

    private void launchTagActivity(long id) {
        Intent intent = new Intent(getActivity(), TagActivity.class);
        Bundle b = new Bundle();
        b.putLong("id", id);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

    private class TaskEventsListener implements SwipeableTaskAdapter.EventListener {

        private List<Task> tasks;

        TaskEventsListener(List<Task> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void onItemRemoved(int position) {
            Task task = tasks.get(position);
            task.setDone(!task.isDone());
            Main.updateTask(task);
        }

        @Override
        public void onItemPinned(int position) {
            Task task = tasks.get(position);
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(task.getDueDate());
            dueDate.add(Calendar.DAY_OF_MONTH, 1);
            task.setDueDate(dueDate.getTime());
            Main.updateTask(task);
        }

        @Override
        public void onItemViewClicked(View v, boolean pinned, int position) {
            launchTaskActivity(tasks.get(position).getId());
        }
    }
}


