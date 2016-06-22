package com.pierrejacquier.olim.data;

import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Task {
    private long id;
    private String title;
    private long tagId;
    private Date dueDate;
    private boolean done;
    private HashMap<String, Object> reminder;

    private Tag tag;

    public Task() {
        this.id = -1;
        this.title = "New task";
        this.tagId = -1;
        this.dueDate = new Date();
        this.done = false;
        this.reminder = null;
        this.tag = null;
    }

    public Task(Cursor cursor) {
        Log.d("Task@31", Arrays.toString(cursor.getColumnNames()));
        this.id = cursor.getLong(0);
        this.title = cursor.getString(1);
        this.dueDate = new Date(cursor.getLong(2));
        this.done = cursor.getLong(3) == 1;
        this.tagId = cursor.getLong(4);
        this.reminder = null;
        this.tag = null;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public Task withTitle(String title) {
        setTitle(title);
        return this;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
    
    public Task withTag(Tag tag) {
        setTag(tag);
        setTagId(tag.getId());
        return this;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tag) {
        this.tagId = tag;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String dispDueDate() {
        return DateFormat.getInstance().getDateInstance().format(dueDate);
    }


    public String dispDueTime() {
        return DateFormat.getInstance().getTimeInstance().format(dueDate);
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    public void setDueDate(long dueDate) {
        setDueDate(new Date(dueDate));
    }

    public Task withDueDate(Date date) {
        setDueDate(date);
        return this;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(long done) {
        setDone(done == 1);
    }
    public void setDone(boolean done) {
        this.done = done;
    }

    public Task withDone(boolean done) {
        setDone(done);
        return this;
    }

    public HashMap<String, Object> getReminder() {
        return reminder;
    }

    public void setReminder(HashMap<String, Object> reminder) {
        this.reminder = reminder;
    }

    public Task withReminder(HashMap<String, Object> reminder) {
        setReminder(reminder);
        return this;
    }

    public Map<String, Object> getObject() {
        Map<String, Object> task = new HashMap<>();
        if (this.title != null) {
           task.put("title", this.title);
        }
        if (this.tagId != -1) {
           task.put("tag", this.tag);
        }
        if (this.dueDate != null) {
           task.put("dueDate", this.dueDate);
        }
        if (this.reminder != null) {
           task.put("reminder", this.reminder);
        }
        task.put("done", this.done);
        return task;
    }
/*
    public void markAsDoneServer() {
        String _id = this.getId();
        if (_id != null) {
            Map<String, Object> update = new HashMap<>();
            update.put("done", true);
            callUpdateTask(_id, update);
        }
    }

    public void markAsNotDoneServer() {
        String _id = this.getId();
        if (_id != null) {
            Map<String, Object> update = new HashMap<>();
            update.put("done", false);
            callUpdateTask(_id, update);
        }
    }

    public void postponeToNextDayServer() {
        String _id = this.getId();
        Calendar newDueDate = Calendar.getInstance();
        newDueDate.setTime(this.getDueDate());
        newDueDate.add(Calendar.DAY_OF_MONTH, 1);
        if (_id != null) {
            Map<String, Object> update = new HashMap<>();
            update.put("dueDate", newDueDate.getTime());
            callUpdateTask(_id, update);
        }
    }

    public void toggleDoneServer() {
        String _id = this.getId();
        if (_id != null) {
            Map<String, Object> update = new HashMap<>();
            update.put("done", !this.isDone());
            callUpdateTask(_id, update);
        }
    }

    public void callUpdateTask(String _id, Map<String, Object> update) {
        MeteorSingleton.getInstance()
            .call("updateTask", new Object[]{ _id, update }, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    Log.d("Success", "test");
                }

                @Override
                public void onError(String error, String reason, String details) {
                    Log.d("Error", error);
                    Log.d("Error", reason);
                }
            });
    }
*/
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);
        Field[] fields = this.getClass().getDeclaredFields();
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {  }
            result.append(newLine);
        }
        result.append("}");
        return result.toString();
    }
}
