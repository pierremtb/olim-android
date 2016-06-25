package com.pierrejacquier.olim.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.adapters.TagsListAdapter;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ActivityTaskBinding;
import com.pierrejacquier.olim.helpers.DbHelper;
import com.pierrejacquier.olim.helpers.Graphics;

import java.util.Calendar;
import java.util.List;

public class TaskActivity extends AppCompatActivity implements View.OnClickListener {

    private Olim app;
    private Task task;
    private List<Tag> tags;
    private long taskId;
    private ActivityTaskBinding binding;
    private ActionBar actionBar;
    private DbHelper dbHelper;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    public MaterialDialog tagsFilteringDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();
        dbHelper = new DbHelper(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task);

        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        final Context context = this;

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTask();
            }
        });
        binding.taskDueDateChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar taskDueDate = Calendar.getInstance();
                taskDueDate.setTime(task.getDueDate());
                int taskYear = taskDueDate.get(Calendar.YEAR);
                int taskMonth = taskDueDate.get(Calendar.MONTH);
                int taskDay = taskDueDate.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int month, int day) {
                                task.setDueDate(year, month, day);
                                binding.setTask(task);

                            }
                        }, taskYear, taskMonth, taskDay);
                datePickerDialog.show();
            }
        });
        binding.taskDueTimeChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar taskDueDate = Calendar.getInstance();
                taskDueDate.setTime(task.getDueDate());
                int taskHour = taskDueDate.get(Calendar.HOUR_OF_DAY);
                int taskMinute = taskDueDate.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour,
                                                  int minute) {
                                task.setDueDate(hour, minute);
                                binding.setTask(task);

                            }
                        }, taskHour, taskMinute, DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        binding.taskTagChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsFilteringDialog.show();
            }
        });

        setTask();
        tags = app.getCurrentUser().getTags();

        createTagsDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionDeleteTask) {
            new MaterialDialog.Builder(this)
                    .title(R.string.delete_task)
                    .positiveText(R.string.delete)
                    .positiveColor(getResources().getColor(R.color.colorPrimary))
                    .negativeText(R.string.cancel)
                    .negativeColor(getResources().getColor(R.color.colorPrimaryText))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            removeTask();
                        }
                    })
                    .show();
            return true;
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tags = app.getCurrentUser().getTags();
        createTagsDialog(this);
        tagsFilteringDialog.show();
    }

    @Override
    public void onClick(View v) {
        Tag tag = (Tag) v.getTag();
        if (getResources().getResourceName(v.getId()).equals("com.pierrejacquier.olim:id/tagEdit")) {
            launchTagActivity(tag.getId());
            tagsFilteringDialog.dismiss();
        } else {
            tagsFilteringDialog.dismiss();
            task.setTag(tag);
            binding.setTask(task);
            applyTaskColor();
            applyTaskIcon();
        }
    }

    @Override
    public void finish() {
        app.getCurrentUser().setTasks(dbHelper.getTasks());
        setResult(0);
        super.finish();
    }

    /**
     * Layout
     */

    private void applyTaskColor() {
        if (task == null) {
            finish();
            return;
        }

        if (task.getTag() == null) {
            return;
        }

        if (task.getTagId() == -1) {
            return;
        }

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(task.getTag().getColor())));
        binding.toolbar2.setBackgroundColor(Color.parseColor(task.getTag().getColor()));
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Graphics.darken(Color.parseColor(task.getTag().getColor()), 0.1));
        }
    }

    private void applyTaskIcon() {
        if (task.getTagId() == -1) {
            return;
        }
        if (task.getTag() == null) {
            return;
        }

        binding.iconTaskIcon
                .setIcon("gmd-" + task.getTag().getIcon().replace("_", "-").replace(" ", "-").toLowerCase());
    }

    private void createTagsDialog(Context context) {
        tagsFilteringDialog = new MaterialDialog.Builder(this)
                .title(R.string.change_tag)
                .autoDismiss(true)
                .positiveText(R.string.clear)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        task.setTag(null);
                        binding.setTask(task);
                        applyTaskColor();
                        applyTaskIcon();
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
                .adapter(new TagsListAdapter(this, tags.toArray(new Tag[tags.size()]), this), null)
                .build();
    }

    /**
     * Data
     */

    private Task getTask() {
        return dbHelper.getTask(taskId);
    }

    private void setTask() {
        taskId = getIntent().getLongExtra("id", -1);

        if (taskId == -1) {
            finish();
            return;
        }

        task = getTask();
        binding.setTask(task);
        applyTaskColor();
        applyTaskIcon();
    }

    private void updateTask() {
        dbHelper.updateTask(task);
        finish();
    }

    private void removeTask() {
        dbHelper.removeTask(task);
        finish();
    }

    /**
     * Navigation
     */

    private void launchTagActivity(long id) {
        Intent intent = new Intent(getApplicationContext(), TagActivity.class);
        Bundle b = new Bundle();
        b.putLong("id", id);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

}
