package com.pierrejacquier.olim.data;

import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.db.Document;
import com.pierrejacquier.olim.helpers.Tools;

public class User {
    private String id;
    private String username;
    private String fullName;
    private String email;

    public User(Document userDoc) {
        HashMap<String, Object> user = Tools.getMap(userDoc);
        ArrayList<Object> emails = (ArrayList<Object>) user.get("emails");
        HashMap<String, Object> profile = (HashMap<String, Object>) Tools.getObject(user, "profile", true);
        HashMap<String, Object> email = (HashMap<String, Object>) emails.get(0);

        this.id = Tools.getString(user, "_id");
        this.username = Tools.getString(user, "username");
        this.fullName = Tools.getString(profile, "fullName");
        this.email = Tools.getString(email, "address");
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public List<Tag> getTags() {
        List <Tag> tags = new ArrayList<>();

        if (this.getId() == null) {
            return tags;
        }

        Document[] tagsDocs = MeteorSingleton.getInstance()
                .getDatabase()
                .getCollection("Tags")
                .whereEqual("owner", this.getId())
                .find();
        for (Document tag : tagsDocs) {
            tags.add(new Tag(tag));
        }
        return tags;
    }

    public List<Task> getTasks() {
        List <Task> tasks = new ArrayList<>();

        if (this.getId() == null) {
            return tasks;
        }

        Document[] tasksDocs = MeteorSingleton.getInstance()
                .getDatabase()
                .getCollection("Tasks")
                .whereEqual("owner", this.getId())
                .find();
        for (Document task : tasksDocs) {
            tasks.add(new Task(task));
        }
        return tasks;
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
