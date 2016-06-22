package com.pierrejacquier.olim.helpers;

/*
 * Created by evan on 4/28/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple SQLiteOpenHelper subclass for demonstration purposes
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VER = 9;

    private static final String DB_NAME = "userData";
    
    private static final String TASKS_TABLE = "tasks";
    private static final String TASKS_ID_COL = "id";
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
            TASKS_ID_COL +  " INTEGER PRIMARY KEY AUTOINCREMENT," +
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

    public void insertTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement insert = makeInsertTaskStatement(db);

        insert.bindString(1, task.getTitle());
        insert.bindLong(2, task.getDueDate().getTime());
        insert.bindLong(3, task.isDone() ? 1 : 0);
        insert.bindLong(4, task.getTagId());
        insert.executeInsert();
        insert.clearBindings();
    }
    
    public void setThisTaskStatus(long id, long status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TASKS_DONE_COL, status);
        db.update(TASKS_TABLE, cv, "id = " + id, null);
    }

    public void insertTag(Tag tag) {
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
                new String[]{TASKS_ID_COL, TASKS_TITLE_COL, TASKS_DUE_DATE_COL, TASKS_DONE_COL, TASKS_TAG_COL},
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

    public List<Task> getTasks() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TASKS_TABLE,
                new String[]{TASKS_ID_COL, TASKS_TITLE_COL, TASKS_DUE_DATE_COL, TASKS_DONE_COL, TASKS_TAG_COL},
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
                    task.setTag(getTag(task.getTagId()));
                }
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return tasks;
    }

    public Tag getTag(long id) {
        Tag tag = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursorTag = db.query(
                TAGS_TABLE,
                new String[]{ TAGS_ID_COL, TAGS_NAME_COL, TAGS_COMMENTS_COL, TAGS_COLOR_COL, TAGS_ICON_COL },
                TAGS_ID_COL + " LIKE ?",
                new String[]{ String.valueOf(id) },
                null,
                null,
                null
        );
        if (cursorTag.moveToLast()) {
            tag = new Tag(cursorTag);
        }
        cursorTag.close();
        return tag;
    }

    public Task getTask(long id) {
        Task task = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursorTask = db.query(
                TASKS_TABLE,
                new String[]{ TASKS_ID_COL, TASKS_TITLE_COL, TASKS_DUE_DATE_COL, TASKS_DONE_COL, TASKS_TAG_COL },
                TASKS_ID_COL + " LIKE ?",
                new String[]{ String.valueOf(id) },
                null,
                null,
                null
        );
        if (cursorTask.moveToLast()) {
            task = new Task(cursorTask);
        }
        cursorTask.close();
        return task;
    }

    public void updateTag(Tag tag) {
        if (tag.getId() == -1) {
            insertTag(tag);
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TAGS_NAME_COL, tag.getName());
        cv.put(TAGS_COMMENTS_COL, tag.getComments());
        cv.put(TAGS_ICON_COL, tag.getIcon());
        cv.put(TAGS_COLOR_COL, tag.getColor());
        db.update(TAGS_TABLE, cv, "id = " + tag.getId(), null);
    }

    public void updateTask(Task task) {
        if (task.getId() == -1) {
            insertTask(task);
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TASKS_TITLE_COL, task.getTitle());
        cv.put(TASKS_DUE_DATE_COL, task.getDueDate().getTime());
        cv.put(TASKS_DONE_COL, task.isDone() ? 1 : 0);
        cv.put(TASKS_TAG_COL, task.getTag() != null ? task.getTag().getId() : -1);
        db.update(TASKS_TABLE, cv, "id = " + task.getId(), null);
    }

    public void removeTag(Tag tag) {
        if (tag.getId() == -1) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TAGS_TABLE, "id = " + tag.getId(), null);
    }

    public void removeTask(Task task) {
        if (task.getId() == -1) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TASKS_TABLE, "id = " + task.getId(), null);
    }

    public List<Tag> getTags() {
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

    private SQLiteStatement makeUpdateTaskStatement(SQLiteDatabase db) {
        String statement =  "UPDATE " + TASKS_TABLE + " SET done = ? WHERE id = ?";
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
