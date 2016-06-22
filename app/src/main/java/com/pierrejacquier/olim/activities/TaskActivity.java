package com.pierrejacquier.olim.activities;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ActivityTaskBinding;
import com.pierrejacquier.olim.helpers.DbHelper;
import com.pierrejacquier.olim.helpers.Graphics;
import com.pierrejacquier.olim.helpers.Tools;

import java.util.Date;

public class TaskActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    private Olim app;
    private Task task;
    private long taskId;
    private ActivityTaskBinding binding;
    private ActionBar actionBar;
    private DbHelper dbHelper;

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

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TaskActivity@57", task.toString());
                updateTask();
            }
        });
        binding.taskDueDateChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        binding.taskDueTimeChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        binding.taskTagChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        setTask();
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
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        //task.setColor(Graphics.intColorToHex(selectedColor));
        binding.setTask(task);
        applyTaskColor();
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
        /*
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(task.getColor())));
        binding.toolbar2.setBackgroundColor(Color.parseColor(task.getColor()));
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Graphics.darken(Color.parseColor(task.getColor()), 0.1));
        }
        */
    }

    private void applyTaskIcon() {
        /*
        binding.iconTaskIcon
                .setIcon("gmd-" + task.getIcon().replace("_", "-").replace(" ", "-").toLowerCase());
                */
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
        } else {
            task = getTask();
        }
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
}
