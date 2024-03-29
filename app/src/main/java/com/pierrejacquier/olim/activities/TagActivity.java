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
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.databinding.ActivityTagBinding;
import com.pierrejacquier.olim.helpers.DbHelper;
import com.pierrejacquier.olim.helpers.Graphics;

import java.util.Random;

public class TagActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    private Olim app;
    private Tag tag;
    private long tagId;
    private ActivityTagBinding binding;
    private ActionBar actionBar;
    private DbHelper dbHelper;
    private ColorChooserDialog colorChooserDialog;
    private MaterialDialog iconChooserDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();
        dbHelper = new DbHelper(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tag);

        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upsertTag();
            }
        });
        binding.tagColorChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorChooserDialog.show(TagActivity.this);
            }
        });
        binding.tagIconChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iconChooserDialog.show();
            }
        });

        setTag();

        colorChooserDialog = new ColorChooserDialog.Builder(this, R.string.tagTitle)
                .doneButton(R.string.md_done_label)
                .cancelButton(R.string.md_cancel_label)
                .backButton(R.string.md_back_label)
                .dynamicButtonColor(true)
                .build();

        iconChooserDialog = new MaterialDialog.Builder(this)
                .title(R.string.tagIcon)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .input("iconname", tag.getIcon(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        tag.setIcon(input.toString());
                        binding.setTag(tag);
                        applyTagIcon();
                    }
                })
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionDeleteTag) {
            new MaterialDialog.Builder(this)
                    .title(R.string.delete_tag)
                    .positiveText(R.string.delete)
                    .positiveColor(getResources().getColor(R.color.colorPrimary))
                    .negativeText(R.string.cancel)
                    .negativeColor(getResources().getColor(R.color.colorPrimaryText))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            removeTag();
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
        tag.setColor(Graphics.intColorToHex(selectedColor));
        binding.setTag(tag);
        applyTagColor();
    }

    @Override
    public void finish() {
        app.getCurrentUser().setTags(dbHelper.getTags());
        setResult(1);
        super.finish();
    }

    /**
     * Layout
     */

    private void applyTagColor() {
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(tag.getColor())));
        binding.toolbar2.setBackgroundColor(Color.parseColor(tag.getColor()));
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Graphics.darken(Color.parseColor(tag.getColor()), 0.1));
        }
    }

    private void applyTagIcon() {
        binding.iconTagIcon
                .setIcon(tag.getIconicsName());
    }

    /**
     * Data
     */

    private Tag getTag() {
        return dbHelper.getTag(tagId);
    }

    private void setTag() {
        tagId = getIntent().getLongExtra("id", -1);
        if (tagId == -1) {
            String[] colors = new String[]{"#F44336","#E91E63","#9C27B0","#673AB7","#3F51B5","#2196F3","#009688","#4CAF50","#FF5722","#795548","#607D8B"};
            int idx = new Random().nextInt(colors.length);
            tag = new Tag().withIcon("label").withColor(colors[idx]);
        } else {
            tag = getTag();
        }
        binding.setTag(tag);
        applyTagColor();
        applyTagIcon();
    }

    private void insertTag() {
        dbHelper.insertTag(tag);
        finish();
    }

    private void updateTag() {
        dbHelper.updateTag(tag);
        finish();
    }

    private void upsertTag() {
        if (tagId == -1) {
            insertTag();
        } else {
            updateTag();
        }
    }

    private void removeTag() {
        dbHelper.removeTag(tag);
        finish();
    }
}
