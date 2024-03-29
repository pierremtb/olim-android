package com.pierrejacquier.olim.data;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
        this.id = cursor.getLong(0);
        this.title = cursor.getString(1);
        this.dueDate = new Date(cursor.getLong(2));
        this.done = cursor.getLong(3) == 1;
        this.tagId = cursor.getLong(4);
        this.reminder = null;
        this.tag = null;
    }

    public Task(String title, Date dueDate) {
        this.id = -1;
        this.title = title;
        this.dueDate = dueDate;
        this.done = false;
        this.tagId = -1;
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
        if (tag == null) {
            this.tagId = -1;
        } else {
            this.tagId = tag.getId();
        }
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
        return DateFormat.getDateInstance(DateFormat.SHORT).format(dueDate);
    }


    public String dispDueTime() {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(dueDate);
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setDueDate(long dueDate) {
        setDueDate(new Date(dueDate));
    }

    public void setDueDate(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.setTime(dueDate);
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        setDueDate(date.getTime());
    }

    public void setDueDate(int hour, int minute) {
        Calendar date = Calendar.getInstance();
        date.setTime(dueDate);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        setDueDate(date.getTime());
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
