package com.pierrejacquier.olim.activities;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.repacked.org.antlr.v4.misc.Utils;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.DbHelper;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.databinding.ActivityTagBinding;
import com.pierrejacquier.olim.helpers.Graphics;

public class TagActivity extends AppCompatActivity {

    private Olim app;
    private Tag tag;
    private long tagId;
    private ActivityTagBinding binding;
    private ActionBar actionBar;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tag);
        Toolbar toolbar = binding.toolbar;
        tagId = getIntent().getLongExtra("id", -1);
        if (tagId== -1) {
            tag = new Tag().withName("Hey").withIcon("add").withComments("anruise").withColor("#000000");
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        actionBar = getSupportActionBar();

        dbHelper = new DbHelper(this);

        binding.setTag(tag);

        applyTagColor();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TagActivity@40", tag.toString());
                applyTagColor();
                dbHelper.putTagInDatabase(tag);
                app.getCurrentUser().setTags(dbHelper.getTagsFromDatabase());
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
            window.setStatusBarColor(Color.parseColor(tag.getColor()));
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
}
