package com.pierrejacquier.olim.data;

/*
 * Created by evan on 4/28/15.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple SQLiteOpenHelper subclass for demonstration purposes
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VER = 8;

    private static final String DB_NAME = "userData";
    
    private static final String TASKS_TABLE = "tasks";
    private static final String TASKS_TITLE_COL = "title";
    private static final String TASKS_DUE_DATE_COL = "due_date";
    private static final String TASKS_DONE_COL = "done";
    private static final String TASKS_TAG_COL = "tag";
    
    private static final String TAGS_TABLE = "tags";
    private static final String TAGS_ID_COL = "id";
    private static final String TAGS_NAME_COL = "name";
    private static final String TAGS_COMMENTS_COL = "comments";
    private static final String TAGS_COLOR_COL = "color";
    private static final String TAGS_ICON_COL = "icon";

    private static final String CREATE_TASKS_TABLE = "CREATE TABLE " + TASKS_TABLE + " (" +
            TASKS_TITLE_COL +  " TEXT," +
            TASKS_DUE_DATE_COL +  " INTEGER," +
            TASKS_DONE_COL +  " INTEGER," +
            TASKS_TAG_COL +  " INTEGER" + ")";

    private static final String CREATE_TAGS_TABLE = "CREATE TABLE " + TAGS_TABLE + " (" +
            TAGS_ID_COL +  " INTEGER PRIMARY KEY AUTOINCREMENT," +
            TAGS_NAME_COL +  " TEXT," +
            TAGS_COMMENTS_COL +  " TEXT," +
            TAGS_COLOR_COL +  " TEXT," +
            TAGS_ICON_COL +  " TEXT" + ")";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TASKS_TABLE);
        db.execSQL(CREATE_TAGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TAGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE);

        onCreate(db);
    }

    public void putTaskInDatabase(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement insert = makeInsertTaskStatement(db);

        insert.bindString(1, task.getTitle());
        insert.bindLong(2, task.getDueDate().getTime());
        insert.bindLong(3, task.isDone() ? 1 : 0);
        insert.bindLong(4, task.getTagId());
        insert.executeInsert();
        insert.clearBindings();
    }

    public void putTagInDatabase(Tag tag) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement insert = makeInsertTagStatement(db);

        insert.bindString(1, tag.getName());
        insert.bindString(2, tag.getComments());
        insert.bindString(3, tag.getColor());
        insert.bindString(4, tag.getIcon());
        insert.executeInsert();
        insert.clearBindings();
    }

    public Task getTaskFromDatabase() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TASKS_TABLE,
                new String[]{TAGS_ID_COL, TASKS_TITLE_COL, TASKS_DUE_DATE_COL, TASKS_DONE_COL, TASKS_TAG_COL},
                null,
                null,
                null,
                null,
                null
        );

        Task task = null;
        if (cursor.moveToLast()) {
            task = new Task(cursor);
        }

        cursor.close();

        return task;
    }

    public Tag getTagFromDatabase() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TAGS_TABLE,
                new String[]{TAGS_ID_COL, TAGS_NAME_COL, TAGS_COMMENTS_COL, TAGS_COLOR_COL, TAGS_ICON_COL},
                null,
                null,
                null,
                null,
                null
        );

        Tag tag = null;
        if (cursor.moveToLast()) {
            tag = new Tag(cursor);
        }

        cursor.close();

        return tag;
    }

    public List<Task> getTasksFromDatabase() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TASKS_TABLE,
                new String[]{TASKS_TITLE_COL, TASKS_DUE_DATE_COL, TASKS_DONE_COL, TASKS_TAG_COL},
                null,
                null,
                null,
                null,
                null
        );

        List<Task> tasks = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(cursor);
                if (task.getTagId() > 0) {
                    Cursor cursorTag = db.query(
                            TAGS_TABLE,
                            new String[]{ TAGS_ID_COL, TAGS_NAME_COL, TAGS_COMMENTS_COL, TAGS_COLOR_COL, TAGS_ICON_COL },
                            TAGS_ID_COL + " LIKE ?",
                            new String[]{ String.valueOf(task.getTagId()) },
                            null,
                            null,
                            null
                    );
                    if (cursorTag.moveToLast()) {
                        task.setTag(new Tag(cursorTag));
                    }
                    cursorTag.close();
                }
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return tasks;
    }

    public List<Tag> getTagsFromDatabase() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TAGS_TABLE,
                new String[]{TAGS_ID_COL, TAGS_NAME_COL, TAGS_COMMENTS_COL, TAGS_COLOR_COL, TAGS_ICON_COL},
                null,
                null,
                null,
                null,
                null
        );

        List<Tag> tags = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                tags.add(new Tag(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return tags;
    }

    private SQLiteStatement makeInsertTaskStatement(SQLiteDatabase db) {
        String statement =  "INSERT OR REPLACE INTO " + TASKS_TABLE + " (" +
                TASKS_TITLE_COL + "," +
                TASKS_DUE_DATE_COL + "," +
                TASKS_DONE_COL + "," +
                TASKS_TAG_COL +
            ") VALUES (?,?,?,?);";
        return db.compileStatement(statement);
    }

    private SQLiteStatement makeInsertTagStatement(SQLiteDatabase db) {
        String statement =  "INSERT OR REPLACE INTO " + TAGS_TABLE + " (" +
                TAGS_NAME_COL + "," +
                TAGS_COMMENTS_COL + "," +
                TAGS_COLOR_COL + "," +
                TAGS_ICON_COL +
            ") VALUES (?,?,?,?);";
        return db.compileStatement(statement);
    }

    // Drop db method
    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TAGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE);
        onCreate(db);
    }
}
