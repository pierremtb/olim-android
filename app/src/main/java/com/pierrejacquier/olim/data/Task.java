package com.pierrejacquier.olim.data;

import android.util.Log;

import com.pierrejacquier.olim.helpers.Tools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.db.Document;

public class Task {
    private String id;
    private String owner;
    private String title;
    private String tag;
    private Date createdAt;
    private Date dueDate;
    private boolean done;
    private HashMap<String, Object> reminder;

    public Task(Document taskDoc) {
        HashMap<String, Object> task = Tools.getMap(taskDoc);

        this.id = Tools.getString(task, "_id");
        this.owner = Tools.getString(task, "owner");
        this.title = Tools.getString(task, "title");
        this.tag = Tools.getString(task, "tag");
        this.createdAt = Tools.getDate(task, "createdAt");
        this.dueDate = Tools.getDate(task, "dueDate");
        this.done = Tools.getBoolean(task, "done");
        this.reminder = Tools.getHashMap(task, "reminder");
    }

    public Task() {
        this.id = null;
        this.owner = null;
        this.title = "New task";
        this.tag = null;
        this.createdAt = null;
        this.dueDate = new Date();
        this.done = false;
        this.reminder = null;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public HashMap<String, Object> getReminder() {
        return reminder;
    }

    public void setReminder(HashMap<String, Object> reminder) {
        this.reminder = reminder;
    }

    public Map<String, Object> getObject() {
        Map<String, Object> task = new HashMap<>();
        if (this.id != null) {
           task.put("_id", this.id);
        }
        if (this.title != null) {
           task.put("title", this.title);
        }
        if (this.owner != null) {
           task.put("owner", this.owner);
        }
        if (this.tag != null) {
           task.put("tag", this.tag);
        }
        if (this.createdAt != null) {
           task.put("createdAt", this.createdAt);
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
