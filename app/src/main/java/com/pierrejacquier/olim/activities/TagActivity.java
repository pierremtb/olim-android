package com.pierrejacquier.olim.activities;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.helpers.DbHelper;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.databinding.ActivityTagBinding;
import com.pierrejacquier.olim.helpers.Graphics;

public class TagActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    private Olim app;
    private Tag tag;
    private long tagId;
    private ActivityTagBinding binding;
    private ActionBar actionBar;
    private DbHelper dbHelper;
    private ColorChooserDialog colorChooserDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();
        dbHelper = new DbHelper(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tag);
        Toolbar toolbar = binding.toolbar;
        tagId = getIntent().getLongExtra("id", -1);
        if (tagId== -1) {
            tag = new Tag().withName("Hey").withIcon("add").withComments("anruise").withColor("#000000");
        } else {
            tag = dbHelper.getTag(tagId);
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        actionBar = getSupportActionBar();


        binding.setTag(tag);

        applyTagColor();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TagActivity@40", tag.toString());
                applyTagColor();
                if (tagId == -1) {
                    dbHelper.putTagInDatabase(tag);
                } else {
                    dbHelper.updateTag(tag);
                }
                app.getCurrentUser().setTags(dbHelper.getTags());
                setResult(0);
                finish();
            }
        });

        colorChooserDialog = new ColorChooserDialog.Builder(this, R.string.tagTitle)
                .doneButton(R.string.md_done_label)
                .cancelButton(R.string.md_cancel_label)
                .backButton(R.string.md_back_label)
                .dynamicButtonColor(true)
                .build();

        binding.tagColorChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorChooserDialog.show(TagActivity.this);
            }
        });
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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
}
