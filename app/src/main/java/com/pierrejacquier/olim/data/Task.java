package com.pierrejacquier.olim.data;

import com.pierrejacquier.olim.helpers.Tools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
        this.reminder = (HashMap<String, Object>) Tools.getObject(task, "reminder", true);
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public boolean isDone() {
        return done;
    }

    public HashMap<String, Object> getReminder() {
        return reminder;
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
